import re

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

functions = []
# Find function signatures
pattern = re.compile(r'\bfun\s+([a-zA-Z0-9_]+)')

for i, line in enumerate(lines):
    # Skip lines that are comments
    if line.strip().startswith("//") or line.strip().startswith("*"):
        continue
    match = pattern.search(line)
    if match:
        func_name = match.group(1)
        # Check if Composable
        is_composable = False
        for j in range(max(0, i-3), i+1):
            if "@Composable" in lines[j]:
                is_composable = True
                break
        functions.append({
            "name": func_name,
            "start_line": i + 1,
            "is_composable": is_composable
        })

# For each function, find its body start and body end
for f_info in functions:
    start_idx = f_info["start_line"] - 1
    # 1. Find the end of parameters by balancing parentheses
    # Start looking from start_idx
    paren_depth = 0
    found_paren = False
    param_end_idx = -1
    
    # We concatenate lines to trace characters
    char_pos = [] # list of (line_num, char)
    for j in range(start_idx, len(lines)):
        line = lines[j]
        # Clean comments
        line_clean = re.sub(r'//.*', '', line)
        for char in line_clean:
            char_pos.append((j, char))
            
    # Now trace characters for parentheses
    for idx, (j, char) in enumerate(char_pos):
        if char == '(':
            paren_depth += 1
            found_paren = True
        elif char == ')':
            paren_depth -= 1
            if found_paren and paren_depth == 0:
                param_end_idx = idx
                break
                
    if param_end_idx == -1:
        # Fallback if parsing failed
        f_info["body_start"] = start_idx
        f_info["end_line"] = len(lines)
        continue
        
    # 2. Find the opening brace of the function body after the closing parenthesis
    body_start_line = -1
    body_start_char_idx = -1
    for k in range(param_end_idx + 1, len(char_pos)):
        line_num, char = char_pos[k]
        if char == '{':
            body_start_line = line_num + 1
            body_start_char_idx = k
            break
            
    if body_start_line == -1:
        # Expressions functions like fun foo() = ...
        f_info["body_start"] = start_idx
        f_info["end_line"] = start_idx + 1
        continue
        
    # 3. Balance braces starting from the function body opening brace
    brace_depth = 1
    body_end_line = len(lines)
    for k in range(body_start_char_idx + 1, len(char_pos)):
        line_num, char = char_pos[k]
        if char == '{':
            brace_depth += 1
        elif char == '}':
            brace_depth -= 1
            if brace_depth == 0:
                body_end_line = line_num + 1
                break
                
    f_info["body_start"] = body_start_line
    f_info["end_line"] = body_end_line

for f in functions:
    if f["name"] in ["HomeScreen", "VlogScreenContent", "CameraScreenContent", "CapturedPreviewScreen", "PermissionsScreen"]:
        print(f"Name: {f['name']} | Start: {f['start_line']} | BodyStart: {f['body_start']} | End: {f['end_line']} | Composable: {f['is_composable']}")
