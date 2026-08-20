import os
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritsModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Add description field
text = text.replace(
    'private final String particle;',
    'private final String particle;\n        private final java.util.List<String> description;'
)
text = text.replace(
    'int actionIntervalTicks, int actionRadius) {',
    'int actionIntervalTicks, int actionRadius, java.util.List<String> description) {'
)
text = text.replace(
    'this.particle = particle;',
    'this.particle = particle;\n            this.description = description;'
)
text = text.replace(
    'public String getParticle() { return particle; }',
    'public String getParticle() { return particle; }\n        public java.util.List<String> getDescription() { return description; }'
)
text = text.replace(
    'sec.getInt(path + "radius", 3)',
    'sec.getInt(path + "radius", 3),\n                            sec.getStringList(path + "description")'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
