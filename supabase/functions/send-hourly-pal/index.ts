import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { GoogleAuth } from "https://deno.land/x/google_deno_auth@v1.2.1/mod.ts"

serve(async (req) => {
  try {
    const projectId = Deno.env.get("FIREBASE_PROJECT_ID")!
    const clientEmail = Deno.env.get("FIREBASE_CLIENT_EMAIL")!
    const privateKey = Deno.env.get("FIREBASE_PRIVATE_KEY")!

    // Compute the current system clock time in IST (GMT+5:30)
    const utcDate = new Date()
    const istOffset = 5.5 * 60 * 60 * 1000 // 5.5 hours in milliseconds
    const istDate = new Date(utcDate.getTime() + istOffset)
    const currentHour = istDate.getUTCHours()
    const timeString = `${currentHour.toString().padStart(2, '0')}:00`

    // Sleep cycle cutoff: 2 AM to 8 AM IST (2, 3, 4, 5, 6, 7)
    const isNightTime = currentHour >= 2 && currentHour < 8
    if (isNightTime) {
      return new Response(JSON.stringify({ message: "Sleep cycle active (2 AM - 8 AM IST). No notifications sent." }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      })
    }

    // Determine target FCM topics to notify based on current hour
    const topicsToNotify: string[] = []
    
    // 1. Hourly topic is always notified during active hours
    topicsToNotify.push("pals_hourly")

    // 2. First pal of the day (8:00 AM IST)
    if (currentHour === 8) {
      topicsToNotify.push("pals_first_time")
    }

    // 3. Three-hourly topic (every 3 hours relative to 8 AM: 8, 11, 14, 17, 20, 23)
    const relativeHour = (currentHour - 8 + 24) % 24
    if (relativeHour % 3 === 0) {
      topicsToNotify.push("pals_three_hourly")
    }

    // Authenticate with Google API endpoints to generate a bearer token
    const auth = new GoogleAuth({
      email: clientEmail,
      key: privateKey,
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    })
    const accessToken = await auth.getAccessToken()

    // Send notifications to all determined topics in parallel
    const sendPromises = topicsToNotify.map(async (topic) => {
      const isFirstPal = topic === "pals_first_time"
      const titleText = isFirstPal ? "Time for your first pal 📹" : `Time for your ${timeString} pal`
      const descText = isFirstPal ? "Start the day with a quick moment." : "Capture this hour before it passes."

      const fcmPayload = {
        message: {
          topic: topic,
          notification: {
            title: titleText,
            body: descText
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
      return response.json()
    })

    const results = await Promise.all(sendPromises)
    return new Response(JSON.stringify({ hour: currentHour, topics: topicsToNotify, results }), {
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