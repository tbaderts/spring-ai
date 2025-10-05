# Quick Reference: MCP Server Tools for OMS Specs

## 🎯 Use This When You Need To...

### Discover What's Available
**"What specs do we have?"**
```
Tool: listDomainDocs()
Returns: List of all .md/.txt files with metadata
```

### See Document Structure  
**"What's in the OMS spec?"**
```
Tool: listDocSections(path="specs/oms_spec.md")
Returns: Outline with all headings and levels
```

### Read Entire Document
**"Show me the whole manifesto"**
```
Tool: readDomainDoc(path="specs/manifesto.md")
Returns: Full document content
```

### Read Just One Section
**"What does section 5 say about domain models?"**
```
Tool: readDocSection(path="specs/oms_spec.md", sectionTitle="Domain Model")
Returns: Just that section and its subsections
```

### Find Documents by Keyword
**"Which docs mention PostgreSQL?"**
```
Tool: searchDomainDocs(query="PostgreSQL", topK=5)
Returns: Top 5 documents with snippets
```

### Find Sections by Keyword
**"Where do specs discuss state machines?"**
```
Tool: searchDocSections(query="state machine", topK=5)
Returns: Top 5 matching sections with titles and snippets
```

---

## 💡 Common Patterns

### Pattern 1: Explore Unknown Territory
```
1. listDomainDocs() → see what files exist
2. listDocSections(path=...) → see structure of interesting file
3. readDocSection(path=..., sectionTitle=...) → read relevant section
```

### Pattern 2: Answer Specific Question
```
1. searchDocSections(query="your topic", topK=5) → find relevant sections
2. readDocSection(path=..., sectionTitle=...) → get full section content
3. Synthesize answer from section(s)
```

### Pattern 3: Deep Dive
```
1. searchDomainDocs(query="broad topic") → find relevant documents
2. listDocSections(path=...) → see document structure  
3. readDocSection → read specific sections of interest
```

---

## 🚀 Example Copilot Queries

Try these in Copilot Chat:

```
@workspace What domain specs are available?

@workspace List all sections in the OMS specification

@workspace What does the OMS spec say about validation?

@workspace Read the Technology Stack section from the OMS spec

@workspace Find all sections that mention Kafka

@workspace Show me the table of contents for the domain model spec

@workspace What are the core domain objects according to the specs?

@workspace Search specs for information about state machines
```

---

## 📁 Your Spec Files

Located in: `/home/tbaderts/data/workspace/oms/specs/`

- **oms_spec.md** - Main OMS State Store specification
- **domain-model_spec.md** - Domain model organization & libraries
- **state-query-store_spec.md** - State store design details
- **streaming_spec.md** - Streaming architecture
- **software-architecture-methodology_spec.md** - Development methodology
- **manifesto.md** - Team manifesto
- **skill_profiles.md** - Skill profiles
- **oms_future_considerations.md** - Future enhancements
- **todo.txt** - TODO items

---

## ⚙️ Configuration

### Current Setup
```yaml
# application.yml
domain:
  docs:
    paths: "oms/specs"  # Scans this directory
```

### Add More Directories
```yaml
domain:
  docs:
    paths: "oms/specs,simulator/specs,/abs/path/to/more/docs"
```

Or via environment variable:
```bash
DOMAIN_DOCS_PATHS="oms/specs,other/docs"
```

---

## 🔧 Troubleshooting

### Tool Not Found
- Reload VS Code window
- Check Copilot MCP config: `~/.config/github-copilot/mcp.json`
- Verify MCP server is running

### Document Not Found
- Use `listDomainDocs()` to see available paths
- Path should be relative: `specs/oms_spec.md` not `/full/path/...`

### Section Not Found
- Use `listDocSections()` first to see available section titles
- Section matching is case-insensitive and fuzzy
- Try just the main words: "Domain Model" instead of "5. Domain Model"

---

## 📊 Cheat Sheet

| What You Want | Tool to Use | Key Parameters |
|---------------|-------------|----------------|
| List all specs | `listDomainDocs` | none |
| Full document | `readDomainDoc` | path |
| Partial document | `readDomainDoc` | path, offset, limit |
| Document outline | `listDocSections` | path |
| One section | `readDocSection` | path, sectionTitle |
| Find documents | `searchDomainDocs` | query, topK |
| Find sections | `searchDocSections` | query, topK |

---

## 🎓 Pro Tips

1. **Start with search** if you don't know which doc to read
2. **List sections first** for unfamiliar documents
3. **Read sections** instead of full docs to save tokens
4. **Use section search** for more precise results than document search
5. **Fuzzy matching** works for section titles - don't need exact match

---

Made with ❤️ for efficient OMS spec navigation in GitHub Copilot
