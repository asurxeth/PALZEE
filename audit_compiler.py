import re
import json

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

# Load states details
states = []
current_state = None
with open("/Users/pratham/.gemini/antigravity-ide/scratch/PAL /state_references.txt", 'r') as f_ref:
    for line in f_ref:
        if line.startswith("Line: "):
            if current_state:
                states.append(current_state)
            parts = line.strip().split(" | ")
            line_num = int(parts[0].split(": ")[1])
            var_name = parts[1].split(": ")[1]
            var_type = parts[2].split(": ")[1]
            owner = parts[3].split(": ")[1]
            current_state = {
                "line": line_num,
                "var": var_name,
                "type": var_type,
                "owner": owner,
                "writes": [],
                "reads": []
            }
        elif line.startswith("  Declaration: "):
            current_state["declaration"] = line.replace("  Declaration: ", "").strip()
        elif line.startswith("    L"):
            # Check if read or write section
            if "Writes" in last_section:
                current_state["writes"].append(line.strip())
            elif "Reads" in last_section:
                current_state["reads"].append(line.strip())
        elif "Writes (" in line:
            last_section = "Writes"
        elif "Reads (" in line:
            last_section = "Reads"
            
    if current_state:
        states.append(current_state)

# Categorize and analyze each state
categorized = {
    "Navigation": [],
    "Chat": [],
    "Camera": [],
    "Playback": [],
    "Upload": [],
    "Authentication": [],
    "Dialogs": [],
    "Animations": [],
    "Permissions": [],
    "Network": [],
    "Database": [],
    "Cache": [],
    "User profile": []
}

# We can categorize by checking the variable names, enclosing functions, and usage.
for s in states:
    var = s["var"]
    func = s["owner"]
    decl = s.get("declaration", "").lower()
    
    # Categorization heuristics
    category = "UI/Local" # default
    
    if var in ["onboardingFlowStep", "selectedTab", "selectedPageIndex", "inMembersSubMenu", "inSettingsSubMenu", "showArchiveView", "tripleDotScreen"]:
        category = "Navigation"
    elif "chat" in var.lower() or "message" in var.lower() or "reply" in var.lower() or "emoji" in var.lower() or var in ["replyInput", "messageInput", "activeReplyPreviewPath", "activeReactionPreview", "showEmojiOverlayForPath"]:
        category = "Chat"
    elif "camera" in var.lower() or "flash" in var.lower() or "recording" in var.lower() or "countdown" in var.lower() or var in ["activeSlot", "activeTimerMode", "videoCaptureRef", "activeRecordingSession", "isCapturingPal", "capturingProgress"]:
        category = "Camera"
    elif "play" in var.lower() or "progress" in var.lower() or "duration" in var.lower() or "mute" in var.lower() or "position" in var.lower() or var in ["vlogPlaybackProgress", "currentPlayingIndex", "isMuted"]:
        category = "Playback"
    elif "upload" in var.lower() or "saving" in var.lower() or "sharing" in var.lower() or "export" in var.lower() or "isSaving" in var or "isExportSavingVideo" in var or "isExportSharingVideo" in var:
        category = "Upload"
    elif "auth" in var.lower() or "login" in var.lower() or var in ["userPin"]:
        category = "Authentication"
    elif "dialog" in var.lower() or "show" in var.lower() or "menu" in var.lower() or "bounds" in var.lower() or "expanded" in var.lower() or var in ["showPlusMenu", "showTripleDotMenu", "showActivityScreen", "showCreatePalFlow", "showJoinPalFlow", "showEditNameDialog", "showDeletePalDialog", "showLeavePalDialog"]:
        category = "Dialogs"
    elif "smile" in var.lower() or "rotation" in var.lower() or "posx" in var.lower() or "posy" in var.lower() or "radius" in var.lower():
        category = "Animations" # or drag gestures
    elif "permission" in var.lower() or "granted" in var.lower():
        category = "Permissions"
    elif "refresh" in var.lower() or "sync" in var.lower() or "loading" in var.lower():
        category = "Network"
    elif var in ["activeHourSubmissions", "dailyHourHistoryMap", "exportMenuDataState", "activeGroupMembersList", "savedVlogPaths"]:
        category = "Database"
    elif "cache" in var.lower() or "restored" in var.lower():
        category = "Cache"
    elif "name" in var.lower() or "avatar" in var.lower() or "first" in var.lower() or "last" in var.lower() or var in ["currentDisplayName", "customAvatarUriString"]:
        category = "User profile"
    else:
        # Fallback based on owner Composable
        if func == "PalChatOverlay":
            category = "Chat"
        elif func in ["CameraPreview", "CameraScreenContent"]:
            category = "Camera"
        elif func == "CapturedPreviewScreen":
            category = "Playback"
        elif func == "PermissionsScreen":
            category = "Permissions"
        elif func == "JoinPalDialogOverlay" or func == "CreatePalDialogOverlay":
            category = "Dialogs"
        else:
            category = "User profile"

    s["category"] = category
    if category in categorized:
        categorized[category].append(s)
    else:
        categorized["User profile"].append(s)

# Analyze write frequencies and recomposition costs
# Drag position states mutates on every drag event (every frame).
# Playback progress mutates on play ticks (every frame or frequently).
# Countdown seconds mutates every second.
# Text inputs mutate on typing (frequently).
# Menus/dialogs mutate on click (rarely/once).
for s in states:
    var = s["var"]
    cat = s["category"]
    
    # Write frequency
    if "posx" in var.lower() or "posy" in var.lower() or "rotation" in var.lower():
        s["freq"] = "every frame"
    elif var in ["vlogPlaybackProgress", "recordingProgress", "progress", "capturingProgress"]:
        s["freq"] = "every frame"
    elif var in ["countdownSeconds"]:
        s["freq"] = "every second"
    elif "input" in var.lower() or "text" in var.lower():
        s["freq"] = "frequently"
    elif var in ["isRefreshing", "isLoadingPals", "isSaving", "isEditingPalLoading", "isExportSavingVideo", "isExportSharingVideo"]:
        s["freq"] = "occasionally"
    elif "show" in var.lower() or "menu" in var.lower() or "dialog" in var.lower():
        s["freq"] = "rarely"
    else:
        s["freq"] = "once" if len(s["writes"]) <= 1 else "rarely"

    # Recomposition scope
    if s["owner"] == "HomeScreen":
        # If read in HomeScreen directly, it recomposes the whole HomeScreen
        # Let's check reads
        direct_read = False
        for r in s["reads"]:
            # Check if read is in when/if in HomeScreen body
            # Simple heuristic: if the line number is under 3907 and not inside another function
            l_num = int(re.search(r'L(\d+):', r).group(1))
            if 1093 <= l_num <= 3907:
                # check if it's inside HomeScreen but not a nested composable call
                direct_read = True
                break
        if direct_read:
            s["scope"] = "HomeScreen (Whole Screen)"
        else:
            s["scope"] = "HomeScreen (Local Callback / Child)"
    else:
        s["scope"] = f"{s['owner']} (Child Composable)"

    # Estimated recompositions triggered
    if s["freq"] == "every frame":
        s["recomps"] = "High (60+ per sec)"
        s["cpu"] = "High"
        s["alloc"] = "High"
    elif s["freq"] == "every second":
        s["recomps"] = "Medium (1 per sec)"
        s["cpu"] = "Medium"
        s["alloc"] = "Low"
    elif s["freq"] == "frequently":
        s["recomps"] = "Medium-High (10-30 per typing session)"
        s["cpu"] = "Medium"
        s["alloc"] = "Medium"
    elif s["freq"] == "occasionally":
        s["recomps"] = "Low"
        s["cpu"] = "Low"
        s["alloc"] = "Low"
    else:
        s["recomps"] = "Very Low"
        s["cpu"] = "Negligible"
        s["alloc"] = "Negligible"

    # Recommendation
    # Position/smile states -> remain local Compose state but should be optimized with Modifier.graphicsLayer (to avoid recomposition entirely)
    # Chat message lists, database maps -> ViewModel StateFlow/SharedFlow
    # Inputs -> ViewModel StateFlow (if validated) or local state
    # Tab/navigation -> ViewModel StateFlow (single source of truth)
    # Upload/network status -> ViewModel StateFlow
    if s["type"] in ["Map", "List", "Set"] or var in ["activeHourSubmissions", "dailyHourHistoryMap", "exportMenuDataState", "activeGroupMembersList"]:
        s["recommendation"] = "StateFlow<T> in ViewModel"
    elif "pos" in var.lower() or "rotation" in var.lower() or "smile" in var.lower():
        s["recommendation"] = "Remain Local State (optimize via graphicsLayer)"
    elif var in ["onboardingFlowStep", "selectedTab"]:
        s["recommendation"] = "StateFlow<T> in ViewModel"
    elif "show" in var.lower() or "dialog" in var.lower() or "menu" in var.lower():
        s["recommendation"] = "StateFlow<Boolean> in ViewModel (for UI events)"
    elif var in ["progress", "vlogPlaybackProgress", "recordingProgress"]:
        s["recommendation"] = "Remain Local State (performance-sensitive)"
    else:
        s["recommendation"] = "StateFlow in ViewModel"

# Print a summary of states by category
for cat, lst in categorized.items():
    print(f"Category: {cat} - Count: {len(lst)}")
