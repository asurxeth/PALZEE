import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { GoogleAuth } from "npm:google-auth-library"

serve(async (req) => {
  try {
    const projectId = Deno.env.get("FIREBASE_PROJECT_ID")
    const clientEmail = Deno.env.get("FIREBASE_CLIENT_EMAIL")
    const privateKeyRaw = Deno.env.get("FIREBASE_PRIVATE_KEY")

    if (!projectId || !clientEmail || !privateKeyRaw) {
      throw new Error("Missing required Firebase environment secrets.")
    }

    let privateKey = privateKeyRaw
    if (privateKey.includes("\\n")) {
      privateKey = privateKey.replace(/\\n/g, "\n")
    }
    if (privateKey.startsWith('"') && privateKey.endsWith('"')) {
      privateKey = privateKey.slice(1, -1)
    }

    const { type, topic, group_name, user_name } = await req.json()

    if (!topic || !type) {
      return new Response(JSON.stringify({ error: "Missing topic or type parameter" }), {
        status: 400,
        headers: { "Content-Type": "application/json" }
      })
    }

    let titleText = ""
    let bodyText = ""

    if (type === "group_join") {
      // Image 1 format: Title = <pals_group_name>, Body = <user_name> joined <pals_group_name>
      titleText = group_name || "Pals Group"
      bodyText = `${user_name || "Someone"} joined ${group_name || "the group"}`
    } else if (type === "new_pal") {
      // Image 2 format: Title = <person_name>, Body = "new pal"
      titleText = user_name || "Palzee User"
      bodyText = "new pal"
    } else {
      return new Response(JSON.stringify({ error: "Invalid notification type" }), {
        status: 400,
        headers: { "Content-Type": "application/json" }
      })
    }

    const auth = new GoogleAuth({
      credentials: {
        client_email: clientEmail,
        private_key: privateKey,
      },
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    })
    const accessToken = await auth.getAccessToken()

    const fcmPayload = {
      message: {
        topic: topic,
        notification: {
          title: titleText,
          body: bodyText
        },
        data: {
          type: type,
          group_name: group_name || "",
          user_name: user_name || ""
        },
        android: {
          notification: {
            icon: "ic_notification",
            color: "#A882D0"
          }
        }
      }
    }

    const response = await fetch(
      `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${accessToken}`
        },
        body: JSON.stringify(fcmPayload)
      }
    )

    const result = await response.json()
    return new Response(JSON.stringify({ success: true, result }), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    })
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" }
    })
  }
})
