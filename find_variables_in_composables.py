import re

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

functions = []
pattern = re.compile(r'\bfun\s+([a-zA-Z0-9_]+)')

for i, line in enumerate(lines):
    if line.strip().startswith("//") or line.strip().startswith("*"):
        continue
    match = pattern.search(line)
    if match:
        func_name = match.group(1)
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
    paren_depth = 0
    found_paren = False
    param_end_idx = -1
    
    char_pos = []
    for j in range(start_idx, len(lines)):
        line = lines[j]
        line_clean = re.sub(r'//.*', '', line)
        for char in line_clean:
            char_pos.append((j, char))
            
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
        f_info["body_start"] = start_idx
        f_info["end_line"] = len(lines)
        continue
        
    body_start_line = -1
    body_start_char_idx = -1
    for k in range(param_end_idx + 1, len(char_pos)):
        line_num, char = char_pos[k]
        if char == '{':
            body_start_line = line_num + 1
            body_start_char_idx = k
            break
            
    if body_start_line == -1:
        f_info["body_start"] = start_idx
        f_info["end_line"] = start_idx + 1
        continue
        
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

states = []
for i, line in enumerate(lines):
    if 'mutableStateOf' in line:
        line_num = i + 1
        # Find which function covers this line
        owner_func = "Global/File"
        is_comp = False
        matching_funcs = []
        for f_info in functions:
            if f_info["body_start"] <= line_num <= f_info["end_line"]:
                matching_funcs.append(f_info)
        if matching_funcs:
            # Sort by range size to get the innermost function
            matching_funcs.sort(key=lambda x: x["end_line"] - x["start_line"])
            owner_func = matching_funcs[0]["name"]
            is_comp = matching_funcs[0]["is_composable"]
            
        # Parse variable name
        var_name = "N/A"
        match_by = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s+by', line)
        match_eq = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s*=', line)
        if match_by:
            var_name = match_by.group(1)
        elif match_eq:
            var_name = match_eq.group(1)
        else:
            if i > 0:
                prev_line = lines[i-1].strip()
                match_prev_by = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s+by', prev_line)
                match_prev_eq = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s*=', prev_line)
                if match_prev_by:
                    var_name = match_prev_by.group(1)
                elif match_prev_eq:
                    var_name = match_prev_eq.group(1)
                    
        states.append({
            "line": line_num,
            "var": var_name,
            "func": owner_func,
            "is_composable": is_comp,
            "content": line.strip()
        })

output_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /mapped_states.txt"
with open(output_path, 'w') as out:
    for s in states:
        out.write(f"Line {s['line']} | Var: {s['var']} | Function: {s['func']} (Composable={s['is_composable']}) | Content: {s['content']}\n")

print(f"Mapped {len(states)} states.")
