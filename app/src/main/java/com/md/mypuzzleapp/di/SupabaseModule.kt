package com.md.mypuzzleapp.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.*
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.realtime

object SupabaseModule {
    // TODO: Replace these with your actual Supabase project credentials
    private const val SUPABASE_URL = "https://zylcdacetfyqfztjndfx.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp5bGNkYWNldGZ5cWZ6dGpuZGZ4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ5MzYwMjcsImV4cCI6MjA3MDUxMjAyN30.Gkl-zzDKgxVEgITDEt4-d6dP_1w2E8-V4lxCLkO2RAs"
    
    val supabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
    
    val auth get() = supabaseClient.auth
    val database get() = supabaseClient.postgrest
    val storage get() = supabaseClient.storage
    val realtime get() = supabaseClient.realtime
}
