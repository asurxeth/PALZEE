import os

root_dir = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL "
dest_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /migration_package.md"

def read_file(relative_path):
    full_path = os.path.join(root_dir, relative_path)
    if not os.path.exists(full_path):
        return f"// File not found: {relative_path}"
    with open(full_path, 'r') as f:
        return f.read()

# 1. Project Tree representation
tree = """
PAL/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/
│               └── com/
│                   └── finrein/
│                       └── pals/
│                           ├── MainActivity.kt
│                           ├── PalApplication.kt
│                           ├── BackgroundSyncService.kt
│                           ├── di/
│                           │   └── NetworkModule.kt
│                           ├── data/
│                           │   └── repository/
│                           │       ├── AuthRepositoryImpl.kt
│                           │       └── GroupRepositoryImpl.kt
│                           │   └── local/
│                           │       └── SessionManager.kt
│                           ├── domain/
│                           │   ├── repository/
│                           │   │   ├── AuthRepository.kt
│                           │   │   └── GroupRepository.kt
│                           │   └── model/
│                           │       └── User.kt
│                           └── presentation/
│                               ├── auth/
│                               │   ├── AuthViewModel.kt
│                               │   ├── AuthUiState.kt
│                               │   ├── AuthUiEvent.kt
│                               │   ├── SimpleAuthScreen.kt
│                               │   └── OnboardingScreen.kt
│                               ├── home/
│                               │   ├── HomeScreen.kt
│                               │   ├── PalGroupGridScreen.kt
│                               │   └── VideoProcessor.kt
│                               └── theme/
│                                   ├── Color.kt
│                                   ├── Spacing.kt
│                                   ├── Theme.kt
│                                   └── Type.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
"""

# Extract requested database functions from HomeScreen.kt
home_screen_content = read_file("app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt")

def extract_function(name, is_suspend=False):
    keyword = "suspend fun" if is_suspend else "fun"
    start_pos = home_screen_content.find(f"{keyword} {name}")
    if start_pos == -1:
        start_pos = home_screen_content.find(f"fun {name}")
    if start_pos == -1:
        return f"// Function {name} not found in HomeScreen.kt"
    
    depth = 0
    started = False
    end_pos = -1
    for idx in range(start_pos, len(home_screen_content)):
        char = home_screen_content[idx]
        if char == '{':
            depth += 1
            started = True
        elif char == '}':
            depth -= 1
            if started and depth == 0:
                end_pos = idx + 1
                break
    if end_pos != -1:
        return home_screen_content[start_pos:end_pos]
    return f"// Parsing failed for function: {name}"

f_refreshPals = extract_function("refreshPals")
f_refreshMessages = extract_function("refreshMessages")
f_refreshActivePalDetails = extract_function("refreshActivePalDetails")
f_uploadFileToSupabase = extract_function("uploadFileToSupabase", is_suspend=True)
f_uploadPalVideoAndGetUrl = extract_function("uploadPalVideoAndGetUrl", is_suspend=True)
f_sendVideoPalToVlog = extract_function("sendVideoPalToVlog", is_suspend=True)
f_ensureVideoCached = extract_function("ensureVideoCached", is_suspend=True)

# Domain models declared in HomeScreen.kt
def extract_class(name):
    start_pos = home_screen_content.find(f"class {name}")
    if start_pos == -1:
        start_pos = home_screen_content.find(f"data class {name}")
    if start_pos == -1:
        return f"// Class {name} not found in HomeScreen.kt"
        
    depth = 0
    started = False
    end_pos = -1
    for idx in range(start_pos, len(home_screen_content)):
        char = home_screen_content[idx]
        if char in ['(', '{']:
            depth += 1
            started = True
        elif char in [')', '}']:
            depth -= 1
            if started and depth == 0:
                end_pos = idx + 1
                break
    if end_pos != -1:
        return home_screen_content[start_pos:end_pos]
    return f"// Parsing failed for class: {name}"

c_PalItem = extract_class("PalItem")
c_UserItem = extract_class("UserItem")
c_UserPalMapping = extract_class("UserPalMapping")
c_SubmissionDbItem = extract_class("SubmissionDbItem")
c_MessageDbItem = extract_class("MessageDbItem")
c_PalDbItem = extract_class("PalDbItem")
c_VlogRecord = extract_class("VlogRecord")

with open(dest_path, 'w') as out:
    out.write("# PAL Architectural Migration Package\n\n")
    
    out.write("## 1. PROJECT TREE\n")
    out.write("```text\n" + tree + "\n```\n\n")
    
    out.write("## 2. SUPABASE INITIALIZATION & NETWORK\n\n")
    out.write("### [PalApplication.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/PalApplication.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/PalApplication.kt") + "\n```\n\n")
    out.write("### [NetworkModule.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/di/NetworkModule.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/di/NetworkModule.kt") + "\n```\n\n")
    
    out.write("## 3. VIEWMODELS\n\n")
    out.write("### [AuthViewModel.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/presentation/auth/AuthViewModel.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/presentation/auth/AuthViewModel.kt") + "\n```\n\n")
    
    out.write("## 4. REPOSITORIES\n\n")
    out.write("### [AuthRepository.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/domain/repository/AuthRepository.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/domain/repository/AuthRepository.kt") + "\n```\n\n")
    out.write("### [GroupRepository.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/domain/repository/GroupRepository.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/domain/repository/GroupRepository.kt") + "\n```\n\n")
    out.write("### [AuthRepositoryImpl.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/data/repository/AuthRepositoryImpl.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/data/repository/AuthRepositoryImpl.kt") + "\n```\n\n")
    out.write("### [GroupRepositoryImpl.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/data/repository/GroupRepositoryImpl.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/data/repository/GroupRepositoryImpl.kt") + "\n```\n\n")
    
    out.write("## 5. DOMAIN MODELS\n\n")
    out.write("### [User.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/domain/model/User.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/domain/model/User.kt") + "\n```\n\n")
    out.write("### Classes declared in [HomeScreen.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt)\n")
    out.write("```kotlin\n")
    out.write(c_PalItem + "\n\n")
    out.write(c_UserItem + "\n\n")
    out.write(c_UserPalMapping + "\n\n")
    out.write(c_SubmissionDbItem + "\n\n")
    out.write(c_MessageDbItem + "\n\n")
    out.write(c_PalDbItem + "\n\n")
    out.write(c_VlogRecord + "\n")
    out.write("```\n\n")
    
    out.write("## 6. DATABASE FUNCTIONS IN HOMESCREEN\n\n")
    out.write("```kotlin\n")
    out.write(f_refreshPals + "\n\n")
    out.write(f_refreshActivePalDetails + "\n\n")
    out.write(f_refreshMessages + "\n\n")
    out.write(f_uploadFileToSupabase + "\n\n")
    out.write(f_uploadPalVideoAndGetUrl + "\n\n")
    out.write(f_ensureVideoCached + "\n\n")
    out.write(f_sendVideoPalToVlog + "\n")
    out.write("```\n\n")
    
    out.write("## 7. NAVIGATION & ENTRY POINT\n\n")
    out.write("### [MainActivity.kt](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/src/main/java/com/finrein/pals/MainActivity.kt)\n")
    out.write("```kotlin\n" + read_file("app/src/main/java/com/finrein/pals/MainActivity.kt") + "\n```\n\n")
    out.write("> [!IMPORTANT]\n")
    out.write("> There is NO `NavHost` or `App()` function inside the project. Navigation is manually operated via `currentUser` nullability check in `MainActivity` content scope.\n\n")
    
    out.write("## 8. ARCHITECTURAL DEPENDENCY DIAGRAM\n\n")
    out.write("```mermaid\ngraph TD\n")
    out.write("  subgraph Current_Architecture [Current Realized Architecture (Highly Coupled)]\n")
    out.write("    UI[HomeScreen.kt / Composables] -->|Direct Query Violation| Supabase[Supabase Client API]\n")
    out.write("    UI -->|Direct DB Mutation Violation| Supabase\n")
    out.write("    UI -->|Google signout/auth directly| Supabase\n")
    out.write("    MainActivity -->|Direct signout/auth directly| Supabase\n")
    out.write("    AuthViewModel -->|Bypasses Repo for Auth OTP| Supabase\n")
    out.write("    AuthViewModel -->|Google auth logic| Supabase\n")
    out.write("    AuthViewModel -->|Call Repo| AuthRepo[AuthRepositoryImpl]\n")
    out.write("    AuthRepo -->|Database query| Supabase\n")
    out.write("  end\n\n")
    out.write("  subgraph Clean_Architecture_Target [Target Architecture]\n")
    out.write("    TargetUI[UI Layer] -->|ReadOnly state observe| TargetVM[ViewModel Layer]\n")
    out.write("    TargetVM -->|Commands / Intent triggers| TargetRepo[Repository Layer]\n")
    out.write("    TargetRepo -->|Postgrest / Storage / Realtime| TargetSupabase[Supabase Client / Data Sources]\n")
    out.write("  end\n\n")
    out.write("  style UI fill:#ff9999,stroke:#333,stroke-width:2px\n")
    out.write("  style AuthViewModel fill:#ffffcc,stroke:#333,stroke-width:1px\n")
    out.write("```\n\n")
  
    out.write("## 9. BUILD CONFIGURATION\n\n")
    out.write("### [build.gradle.kts (Project-Level)](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/build.gradle.kts)\n")
    out.write("```kotlin\n" + read_file("build.gradle.kts") + "\n```\n\n")
    out.write("### [build.gradle.kts (App-Level)](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/app/build.gradle.kts)\n")
    out.write("```kotlin\n" + read_file("app/build.gradle.kts") + "\n```\n\n")
    out.write("### [libs.versions.toml](file:///Users/pratham/.gemini/antigravity-ide/scratch/PAL%20/gradle/libs.versions.toml)\n")
    out.write("```toml\n" + read_file("gradle/libs.versions.toml") + "\n```\n\n")
    
    out.write("## 10. DATABASE SCHEMAS, CONSTRAINTS & ROUTINES\n\n")
    out.write("### Table Definitions\n")
    out.write("```sql\n")
    out.write("-- 1. pals Table\n")
    out.write("CREATE TABLE public.pals (\n")
    out.write("    pal_code VARCHAR PRIMARY KEY NOT NULL UNIQUE,\n")
    out.write("    name VARCHAR NOT NULL,\n")
    out.write("    size VARCHAR DEFAULT '3',\n")
    out.write("    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())\n")
    out.write(");\n\n")
    out.write("-- 2. user_pals (Group Memberships & Vlogs Tracker)\n")
    out.write("CREATE TABLE public.user_pals (\n")
    out.write("    id BIGSERIAL PRIMARY KEY,\n")
    out.write("    user_id UUID NOT NULL,\n")
    out.write("    pal_code VARCHAR NOT NULL REFERENCES public.pals(pal_code) ON DELETE CASCADE,\n")
    out.write("    video_url VARCHAR NULL, -- Used to store individual vlog uploads\n")
    out.write("    user_display_name VARCHAR NULL,\n")
    out.write("    user_avatar_url VARCHAR NULL,\n")
    out.write("    joined_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())\n")
    out.write(");\n\n")
    out.write("-- 3. submissions (Hour buckets submissions)\n")
    out.write("CREATE TABLE public.submissions (\n")
    out.write("    id BIGSERIAL PRIMARY KEY,\n")
    out.write("    pal_code VARCHAR NOT NULL REFERENCES public.pals(pal_code) ON DELETE CASCADE,\n")
    out.write("    user_id UUID NOT NULL,\n")
    out.write("    user_display_name VARCHAR NOT NULL,\n")
    out.write("    image_url VARCHAR NOT NULL,\n")
    out.write("    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())\n")
    out.write(");\n\n")
    out.write("-- 4. messages (Chat overlay data)\n")
    out.write("CREATE TABLE public.messages (\n")
    out.write("    id BIGSERIAL PRIMARY KEY,\n")
    out.write("    pal_code VARCHAR NOT NULL REFERENCES public.pals(pal_code) ON DELETE CASCADE,\n")
    out.write("    user_id UUID NOT NULL,\n")
    out.write("    message_text TEXT NOT NULL,\n")
    out.write("    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())\n")
    out.write(");\n")
    out.write("```\n\n")
    out.write("### RPC Definitions\n")
    out.write("```sql\n")
    out.write("-- RPC 1: generate_unique_pal_code\n")
    out.write("CREATE OR REPLACE FUNCTION public.generate_unique_pal_code()\n")
    out.write("RETURNS VARCHAR AS $$\n")
    out.write("DECLARE\n")
    out.write("    new_code VARCHAR;\n")
    out.write("    exists_flag BOOLEAN;\n")
    out.write("BEGIN\n")
    out.write("    LOOP\n")
    out.write("        new_code := upper(substring(md5(random()::text) from 1 for 6));\n")
    out.write("        SELECT EXISTS(SELECT 1 FROM public.pals WHERE pal_code = new_code) INTO exists_flag;\n")
    out.write("        IF NOT exists_flag THEN\n")
    out.write("            RETURN new_code;\n")
    out.write("        END IF;\n")
    out.write("    END LOOP;\n")
    out.write("END;\n")
    out.write("$$ LANGUAGE plpgsql;\n\n")
    out.write("-- RPC 2: get_clean_homescreen_dashboard\n")
    out.write("CREATE OR REPLACE FUNCTION public.get_clean_homescreen_dashboard(current_user_uuid UUID)\n")
    out.write("RETURNS JSONB AS $$\n")
    out.write("DECLARE\n")
    out.write("    vlog_size INT;\n")
    out.write("    groups_json JSONB;\n")
    out.write("BEGIN\n")
    out.write("    -- Count user's personal vlog entries\n")
    out.write("    SELECT count(*) INTO vlog_size FROM public.submissions WHERE pal_code = 'vlog' AND user_id = current_user_uuid;\n")
    out.write("    \n")
    out.write("    -- Fetch and map pals groups user belongs to\n")
    out.write("    SELECT jsonb_agg(jsonb_build_object(\n")
    out.write("        'code', p.pal_code,\n")
    out.write("        'name', p.name,\n")
    out.write("        'size', p.size,\n")
    out.write("        'is_creator', EXISTS(SELECT 1 FROM public.user_pals up WHERE up.pal_code = p.pal_code AND up.user_id = current_user_uuid)\n")
    out.write("    )) INTO groups_json\n")
    out.write("    FROM public.pals p\n")
    out.write("    JOIN public.user_pals up ON p.pal_code = up.pal_code\n")
    out.write("    WHERE up.user_id = current_user_uuid;\n\n")
    out.write("    RETURN jsonb_build_object(\n")
    out.write("        'vlog_box_size', COALESCE(vlog_size::text, '0'),\n")
    out.write("        'groups', COALESCE(groups_json, '[]'::jsonb)\n")
    out.write("    );\n")
    out.write("END;\n")
    out.write("$$ LANGUAGE plpgsql;\n")
    out.write("```\n\n")
    out.write("### RLS, Indexes & Foreign Keys\n")
    out.write("```sql\n")
    out.write("-- RLS enabling\n")
    out.write("ALTER TABLE public.pals ENABLE ROW LEVEL SECURITY;\n")
    out.write("ALTER TABLE public.user_pals ENABLE ROW LEVEL SECURITY;\n")
    out.write("ALTER TABLE public.submissions ENABLE ROW LEVEL SECURITY;\n")
    out.write("ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;\n\n")
    out.write("-- Enable realtime replication for updates stream listening\n")
    out.write("alter publication supabase_realtime add table public.user_pals;\n")
    out.write("alter publication supabase_realtime add table public.submissions;\n")
    out.write("alter publication supabase_realtime add table public.messages;\n\n")
    out.write("-- Indexes\n")
    out.write("CREATE INDEX idx_user_pals_user ON public.user_pals(user_id);\n")
    out.write("CREATE INDEX idx_user_pals_code ON public.user_pals(pal_code);\n")
    out.write("CREATE INDEX idx_submissions_code ON public.submissions(pal_code);\n")
    out.write("CREATE INDEX idx_messages_code ON public.messages(pal_code);\n")
    out.write("```\n")

print("Migration package written to migration_package.md")
