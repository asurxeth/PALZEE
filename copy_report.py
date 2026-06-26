import os

source_path = "/Users/pratham/.gemini/antigravity-ide/scratch/PAL /state_mutation_audit.md"
dest_dir = "/Users/pratham/.gemini/antigravity-ide/brain/4c50b297-5141-484c-8e39-cb0bca7b296e"
dest_path = os.path.join(dest_dir, "state_mutation_audit.md")

with open(source_path, 'r') as f_in:
    content = f_in.read()

# Make sure directory exists (it should be automatically created but good practice)
os.makedirs(dest_dir, exist_ok=True)

with open(dest_path, 'w') as f_out:
    f_out.write(content)

print(f"Copied audit report to {dest_path}")
