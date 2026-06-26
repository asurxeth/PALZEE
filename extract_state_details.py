import re

file_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /app/src/main/java/com/finrein/pals/presentation/home/HomeScreen.kt"

with open(file_path, 'r') as f:
    lines = f.readlines()

# Load the mapped states
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

# Analyze reads/writes for each state
# We can search the entire file for the variable name.
# Writes are typically: varName = ..., varName.value = ..., varName.mutate { ... }
# Reads are other usages.
for s in mapped_states:
    var_name = s["var"]
    if var_name == "N/A" or not var_name:
        s["reads"] = ["Unknown (no var name)"]
        s["writes"] = ["Unknown (no var name)"]
        s["type"] = "Unknown"
        continue
        
    reads = []
    writes = []
    
    # Simple regexes to find writes and reads
    # Write: var_name followed by optional spaces and '='
    # Or var_name.value =
    write_re = re.compile(r'\b' + re.escape(var_name) + r'\b\s*(?:\.value\s*)?=[^=]')
    # Or other mutating operators like +=, -=, etc.
    mutate_re = re.compile(r'\b' + re.escape(var_name) + r'\b\s*(?:\.value\s*)?(?:\+=|-=|\.clear\(|\.add\(|\.put\(|\.remove\(|\.update\s*\{)')
    
    # Read: any occurrence not in write/mutate
    ref_re = re.compile(r'\b' + re.escape(var_name) + r'\b')
    
    for l_idx, line_content in enumerate(lines):
        line_num = l_idx + 1
        # Skip the declaration line for read/write checks
        if line_num == s["line"]:
            continue
        
        # Check if line contains variable name
        if ref_re.search(line_content):
            # Check if comment or import
            if line_content.strip().startswith("//") or line_content.strip().startswith("*") or "import " in line_content:
                continue
                
            is_write = False
            if write_re.search(line_content) or mutate_re.search(line_content):
                is_write = True
                
            snippet = f"L{line_num}: {line_content.strip()}"
            if is_write:
                writes.append(snippet)
            else:
                reads.append(snippet)
                
    s["reads"] = reads
    s["writes"] = writes
    
    # Infer type from content
    content = s["content"]
    # Check if mutableStateOf<Type>
    type_match = re.search(r'mutableStateOf<([^>]+)>', content)
    if type_match:
        s["type"] = type_match.group(1)
    else:
        # Infer from value
        val_match = re.search(r'mutableStateOf\((.*)\)', content)
        if val_match:
            val = val_match.group(1).strip()
            if val.startswith('"') or val.startswith('""'):
                s["type"] = "String"
            elif val.lower() == "false" or val.lower() == "true":
                s["type"] = "Boolean"
            elif val.isdigit():
                s["type"] = "Int"
            elif val.endswith('f') or val.replace('.', '', 1).isdigit():
                if 'f' in val:
                    s["type"] = "Float"
                else:
                    s["type"] = "Double"
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

# Write out to detailed report
output_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /state_references.txt"
with open(output_path, 'w') as out:
    for s in mapped_states:
        out.write(f"Line: {s['line']} | Var: {s['var']} | Type: {s['type']} | Owner: {s['func']}\n")
        out.write(f"  Declaration: {s['content']}\n")
        out.write(f"  Writes ({len(s['writes'])}):\n")
        for w in s['writes'][:10]:  # Limit to first 10 for visibility
            out.write(f"    {w}\n")
        if len(s['writes']) > 10:
            out.write(f"    ... and {len(s['writes']) - 10} more\n")
        out.write(f"  Reads ({len(s['reads'])}):\n")
        for r in s['reads'][:10]:
            out.write(f"    {r}\n")
        if len(s['reads']) > 10:
            out.write(f"    ... and {len(s['reads']) - 10} more\n")
        out.write("-" * 80 + "\n")

print(f"Details extracted for {len(mapped_states)} states.")
