// supabase/edge-functions/send-monthly-newsletter/index.ts
//
// Deploy with:  supabase functions deploy send-monthly-newsletter
// Schedule with Supabase's Cron Jobs (Dashboard -> Edge Functions -> Schedules),
// e.g. "0 9 1 * *" to run at 09:00 on the 1st of every month.
//
// This function runs server-side, so it's the ONLY place that should ever use
// SUPABASE_SECRET_KEY (the service_role key). It must never be embedded in the
// Android app — the app only ever uses SUPABASE_PUBLISHABLE_KEY (anon key).
//
// Wire in a real email provider (Resend, Postmark, SendGrid, etc.) where marked.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SECRET_KEY")!; // set as an Edge Function secret, never committed
const EMAIL_PROVIDER_API_KEY = Deno.env.get("EMAIL_PROVIDER_API_KEY"); // e.g. Resend API key

Deno.serve(async (_req) => {
  const supabase = createClient(SUPABASE_URL, SERVICE_ROLE_KEY);

  const { data: subscribers, error } = await supabase
    .from("newsletter_subscriptions")
    .select("email")
    .eq("active", true);

  if (error) {
    return new Response(JSON.stringify({ error: error.message }), { status: 500 });
  }

  let sent = 0;
  for (const sub of subscribers ?? []) {
    // TODO: replace with a real call to your email provider's API, e.g.:
    // await fetch("https://api.resend.com/emails", {
    //   method: "POST",
    //   headers: {
    //     Authorization: `Bearer ${EMAIL_PROVIDER_API_KEY}`,
    //     "Content-Type": "application/json",
    //   },
    //   body: JSON.stringify({
    //     from: "MyDocx <newsletter@yourdomain.com>",
    //     to: sub.email,
    //     subject: "أبرز ما نُشر هذا الشهر على MyDocx",
    //     html: "<p>...</p>",
    //   }),
    // });
    sent++;
  }

  return new Response(JSON.stringify({ sent }), {
    headers: { "Content-Type": "application/json" },
  });
});
