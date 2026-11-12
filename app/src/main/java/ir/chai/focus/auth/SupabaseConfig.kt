package ir.chai.focus.auth

/**
 * پیکربندی Supabase — کلید عمومی است و داخل اپ قرار می‌گیرد (امن).
 * کلید سرویس (secret) هرگز داخل اپ نمی‌آید؛ فقط در Edge Functions است.
 */
object SupabaseConfig {
    const val URL = "https://jgeqpxhkltxtcbccywft.supabase.co"
    const val PUBLISHABLE_KEY = "sb_publishable_XlzV1SDwcv3kNu5Dpj_bdA_dXnUJQm2"

    /** نام توابع Edge (اسلاگ‌ها در جدول function_routes نگه داشته می‌شوند) */
    object Fn {
        const val REGISTER = "register"
        const val LOGIN = "login"
        const val CHECK_USERNAME = "check-username"
    }
}
