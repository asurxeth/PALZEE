import re
import json

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

# Read previous mapped states
mapped_states = []
state_pattern = re.compile(r'Line (\d+) \| Var: ([a-zA-Z0-9_]+|N/A) \| Function: ([a-zA-Z0-9_/]+) \((?:Composable=(True|False))\) \| Content: (.*)')

with open("/Users/pratham/.gemini/antigravity-ide/scratch/PAL /mapped_states.txt", 'r') as f_mapped:
    for line in f_mapped:
        m = state_pattern.match(line.strip())
        if m:
            mapped_states.append({
                "line": int(m.group(1)),
                "var": m.group(2),
                "func": m.group(3),
                "is_composable": m.group(4) == "True",
                "content": m.group(5)
            })

# Let's extract reader and writer info. We will do a full scan for each variable name.
for s in mapped_states:
    var_name = s["var"]
    if var_name == "N/A" or not var_name:
        s["readers"] = ["N/A"]
        s["writes"] = ["N/A"]
        s["type"] = "Any"
        continue
        
    reads = []
    writes = []
    
    # regexes
    write_re = re.compile(r'\b' + re.escape(var_name) + r'\b\s*(?:\.value\s*)?=[^=]')
    mutate_re = re.compile(r'\b' + re.escape(var_name) + r'\b\s*(?:\.value\s*)?(?:\+=|-=|\.clear\(|\.add\(|\.put\(|\.remove\(|\.update\s*\{)')
    ref_re = re.compile(r'\b' + re.escape(var_name) + r'\b')
    
    for l_idx, line_content in enumerate(lines):
        line_num = l_idx + 1
        if line_num == s["line"]:
            continue
        if ref_re.search(line_content):
            if line_content.strip().startswith("//") or line_content.strip().startswith("*") or "import " in line_content:
                continue
                
            # Find enclosing function of reader/writer
            # search backwards for fun definition
            ref_func = "HomeScreen"
            for f_idx in range(l_idx, -1, -1):
                f_line = lines[f_idx].strip()
                if f_line.startswith("fun ") or " fun " in f_line:
                    ref_func = f_line.split("fun ")[1].split("(")[0].strip()
                    break
                    
            is_write = False
            if write_re.search(line_content) or mutate_re.search(line_content):
                is_write = True
                
            if is_write:
                writes.append(f"{ref_func} (L{line_num})")
            else:
                reads.append(f"{ref_func} (L{line_num})")
                
    s["readers"] = sorted(list(set(reads))) if reads else ["N/A"]
    s["writes"] = sorted(list(set(writes))) if writes else ["N/A"]
    
    # Infer type from content
    content = s["content"]
    type_match = re.search(r'mutableStateOf<([^>]+)>', content)
    if type_match:
        s["type"] = type_match.group(1)
    else:
        val_match = re.search(r'mutableStateOf\((.*)\)', content)
        if val_match:
            val = val_match.group(1).strip()
            if val.startswith('"') or val.startswith('""') or val.startswith("TextFieldValue"):
                s["type"] = "String"
            elif val.lower() == "false" or val.lower() == "true":
                s["type"] = "Boolean"
            elif val.isdigit():
                s["type"] = "Int"
            elif val.endswith('f') or val.replace('.', '', 1).isdigit():
                s["type"] = "Float" if 'f' in val else "Double"
            elif val.startswith("emptyMap") or "mapOf" in val:
                s["type"] = "Map"
            elif val.startswith("emptyList") or "listOf" in val:
                s["type"] = "List"
            elif val.startswith("emptySet") or "setOf" in val:
                s["type"] = "Set"
            elif "dp" in val:
                s["type"] = "Dp"
            else:
                s["type"] = "Inferred (" + val + ")"
        else:
            s["type"] = "Unknown"

# Determine target location (UI, ViewModel, Repository, Manager)
for s in mapped_states:
    var = s["var"]
    func = s["func"]
    
    # Classify category
    category = "User profile"
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
        category = "Animations"
    elif "permission" in var.lower() or "granted" in var.lower():
        category = "Permissions"
    elif "refresh" in var.lower() or "sync" in var.lower() or "loading" in var.lower():
        category = "Network"
    elif var in ["activeHourSubmissions", "dailyHourHistoryMap", "exportMenuDataState", "activeGroupMembersList", "savedVlogPaths"]:
        category = "Database"
    elif "cache" in var.lower() or "restored" in var.lower():
        category = "Cache"
        
    s["category"] = category

    # Determine ownership target
    if category in ["Animations"]:
        s["target"] = "UI"
    elif var in ["plusMenuBounds", "tripleDotMenuBounds", "joinPalBounds", "editNameBounds", "screenCornerRadius", "activeCamera", "videoCaptureRef", "activeRecordingSession", "bitmap", "resolvedPaths", "isVideoReady", "isPlaying", "progress", "vlogPlaybackProgress", "recordingProgress", "duration", "currentPosition", "replyInput", "messageInput", "editCaptionText"]:
        s["target"] = "UI"
    elif "pos" in var.lower() or "rotation" in var.lower() or "smile" in var.lower():
        s["target"] = "UI"
    elif "bounds" in var.lower() or "radius" in var.lower():
        s["target"] = "UI"
    else:
        s["target"] = "ViewModel"

# Write final report
output_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /state_audit_final.json"
with open(output_path, 'w') as out:
    json.dump(mapped_states, out, indent=2)

print("Final audit JSON generated.")
