import re

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

def get_enclosing_function(index):
    # Search backwards for a function signature
    for i in range(index, -1, -1):
        line = lines[i].strip()
        if line.startswith("fun ") or " fun " in line:
            # check if there's @Composable on this or previous lines
            is_composable = False
            for j in range(max(0, i-3), i+1):
                if "@Composable" in lines[j]:
                    is_composable = True
                    break
            func_name = line.split("fun ")[1].split("(")[0].strip()
            return f"{func_name} (Composable={is_composable})"
    return "Unknown"

results = []
for i, line in enumerate(lines):
    if 'mutableStateOf' in line:
        line_num = i + 1
        stripped = line.strip()
        
        # Try to extract variable name
        var_name = "N/A"
        match_by = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s+by', line)
        match_eq = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s*=', line)
        if match_by:
            var_name = match_by.group(1)
        elif match_eq:
            var_name = match_eq.group(1)
        else:
            # Check previous line if it was multi-line declaration
            if i > 0:
                prev_line = lines[i-1].strip()
                match_prev_by = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s+by', prev_line)
                match_prev_eq = re.search(r'(?:val|var)\s+([a-zA-Z0-9_]+)\s*=', prev_line)
                if match_prev_by:
                    var_name = match_prev_by.group(1)
                elif match_prev_eq:
                    var_name = match_prev_eq.group(1)
                    
        func = get_enclosing_function(i)
        results.append({
            "line": line_num,
            "var": var_name,
            "func": func,
            "content": stripped
        })

output_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /parsed_details.txt"
with open(output_path, 'w') as out:
    for r in results:
        out.write(f"Line {r['line']} | Var: {r['var']} | Func: {r['func']} | Content: {r['content']}\n")

print(f"Parsed {len(results)} states.")
