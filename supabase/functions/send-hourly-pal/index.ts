import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { GoogleAuth } from "https://deno.land/x/google_deno_auth@v1.2.1/mod.ts"

serve(async (req) => {
  try {
    // 1. Fetch encrypted environmental keys straight from your vault parameters
    const projectId = Deno.env.get("FIREBASE_PROJECT_ID")!
    const clientEmail = Deno.env.get("FIREBASE_CLIENT_EMAIL")!
    const privateKey = Deno.env.get("FIREBASE_PRIVATE_KEY")!

    // 2. Compute the current system clock time, flooring the minutes cleanly
    const now = new Date()
    const flooredHour = now.getHours().toString().padStart(2, '0')
    const timeString = `${flooredHour}:00`

    // 3. Authenticate with Google API endpoints to generate a bearer token
    const auth = new GoogleAuth({
      email: clientEmail,
      key: privateKey,
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    })
    const accessToken = await auth.getAccessToken()

    // 4. Construct the exact payload targeting your application topic channel
    const fcmPayload = {
      message: {
        topic: "all_pals", 
        notification: {
          title: `Time for your ${timeString} pal`,
          body: "Capture this hour before it passes."
        },
        android: {
          notification: {
            icon: "ic_launcher_custom", // Maps to your system layout asset
            color: "#A882D0" // Your brand lavender accent color profile
          }
        }
      }
    }

    // 5. Fire the compiled transmission block straight over to the FCM servers
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

    const responseData = await response.json()
    return new Response(JSON.stringify(responseData), { 
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