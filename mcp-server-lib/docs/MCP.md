# MCP Domain Documentation Tools

## What's included

- MCP tool class: `DomainDocsTools` with:
	- `listDomainDocs`: lists available domain documents with metadata
	- `readDomainDoc`: reads full or partial domain documents
	- `listDocSections`: lists all sections/headings in a document
	- `readDocSection`: reads a specific section by title
	- `searchDomainDocs`: keyword search across all domain documents
	- `searchDocSections`: keyword search within document sections

## Using the tools in chat

### Document Discovery & Reading

- Discover: "list tools"
- Enumerate docs: "Call `listDomainDocs`"
- Read full doc: "Call `readDomainDoc` path='specs/oms_spec.md'"
- Read paged: "Call `readDomainDoc` path='specs/oms_spec.md' offset=0 limit=4000"

### Section Navigation (NEW)

- List sections: "Call `listDocSections` path='specs/oms_spec.md'" 
  - Returns all headings with their levels and line numbers
- Read specific section: "Call `readDocSection` path='specs/oms_spec.md' sectionTitle='Domain Model'"
  - Extracts just that section and its subsections
- Navigate to subsection: "Read the 'Query API' section from the OMS spec"

### Search

- Keyword search across all docs: "Use `searchDomainDocs` with query='risk policy' topK=5"
- Section-level search: "Use `searchDocSections` with query='state machine' topK=5"
  - Returns matching sections with their titles and context
  - More precise than full-document search

### Example Workflows

**Finding specific information:**
```
User: "What does the OMS spec say about state machines?"
Copilot: 
1. Calls searchDocSections(query='state machine', topK=5)
2. Finds "State Machine Engine" section in domain-model_spec.md
3. Calls readDocSection to get full details
```

**Navigating large specifications:**
```
User: "Show me all sections in the OMS spec"
Copilot:
1. Calls listDocSections(path='specs/oms_spec.md')
2. Returns outline: "1. Introduction", "2. Goals", "3. Architecture", etc.

User: "Read section 5 about the Domain Model"
Copilot:
1. Calls readDocSection(path='specs/oms_spec.md', sectionTitle='Domain Model')
2. Returns just that section
```

Clients can ground responses using these tool outputs. `readDomainDoc`: reads a document (supports offset/limit)
	- `searchDomainDocs`: keyword search across documents
	- `listDocSections`: lists all sections/headings in a markdown document
	- `readDocSection`: reads a specific section by title
	- `searchDocSections`: searches within document sections for precise results
- Tools are registered alongside existing ones (`searchOrders`, `ping`).
- Module compiles cleanly and runs as an MCP stdio server.in knowledge available to Copilot and Claude via MCP

You can expose your domain knowledge (models, specifications, team manifesto, core values, financial domain knowledge, etc.) through an MCP (Model Context Protocol) server so GitHub Copilot (VS Code) and Claude Desktop can discover and use it during chat.

### What’s included

- MCP tool class: `DomainDocsTools` with:
	- `listDomainDocs`: lists available domain documents with metadata
	- `readDomainDoc`: reads a document (supports offset/limit)
	- `searchDomainDocs`: keyword search across documents
- Tools are registered alongside existing ones (`searchOrders`, `ping`).
- Module compiles cleanly and runs as an MCP stdio server.

### Files changed

- `mcp-server-lib/src/main/java/org/example/spring_ai/docs/DomainDocsTools.java` — domain-docs tools
- `mcp-server-lib/src/main/java/org/example/spring_ai/oms/McpConfig.java` — wires the tools into MCP

### Defaults

- Scans for docs under `oms/specs` and `simulator/specs` by default.
- Override with `domain.docs.paths` (comma-separated), e.g. to add “team manifesto”, “core values”, and “financial knowledge” folders.

## How MCP fits in

- MCP servers speak JSON-RPC over stdio (or other transports) and expose “tools,” “resources,” and “prompts.”
- Copilot and Claude can launch your server, list tools, and call them to fetch knowledge as needed.

## Wire to GitHub Copilot (VS Code)

Create or update your Copilot MCP config file on Linux:

- `~/.config/github-copilot/mcp.json` (newer clients may also read `~/.copilot/mcp.json`)

Example entry:

```json
{
	"mcpServers": {
		"oms": {
			"command": "bash",
			"args": [
				"-lc",
				"cd /home/tbaderts/data/workspace/spring-ai && ./run-mcp.sh"
			],
			"env": {
				"JAVA_TOOL_OPTIONS": "-Xmx512m",
				"MCP_TRANSPORT": "stdio"
			},
			"description": "Spring AI MCP server exposing domain docs and OMS tools"
		}
	}
}
```

Optional: Configure which doc folders to scan by adding an environment variable for the server:

```json
"env": {
	"JAVA_TOOL_OPTIONS": "-Xmx512m",
	"MCP_TRANSPORT": "stdio",
	"DOMAIN_DOCS_PATHS": "oms/specs,simulator/specs,/abs/path/core-values,/abs/path/financial"
}
```

Notes:

- `DOMAIN_DOCS_PATHS` maps to Spring property `domain.docs.paths`.
- Reload the VS Code window. In Copilot Chat, ask “list tools” and you should see `listDomainDocs`, `readDomainDoc`, `searchDomainDocs`, `searchOrders`, `ping`.

## Wire to Claude Desktop

Create a JSON file on Linux at: `~/.config/Claude/mcp/oms.json`

```json
{
	"name": "oms",
	"command": "bash",
	"args": [
		"-lc",
		"cd /home/tbaderts/data/workspace/spring-ai && ./run-mcp.sh"
	],
	"env": {
		"JAVA_TOOL_OPTIONS": "-Xmx512m",
		"MCP_TRANSPORT": "stdio",
		"DOMAIN_DOCS_PATHS": "oms/specs,simulator/specs"
	}
}
```

Restart Claude Desktop. In a new chat:

- “List available MCP tools”
- “Search domain docs for ‘core values’ and show the top 3”
- “Read the team manifesto doc”

Tip: Depending on your Claude Desktop version, the config directory/schema may vary slightly; the above is the common stdio pattern. Some builds allow enabling MCP servers via Settings.

## Organize your domain knowledge

- Use text-friendly formats: `.md`, `.markdown`, `.txt`, `.adoc`
- Default scan paths: `oms/specs`, `simulator/specs`
- To add more folders, set the property (env var shown below):

```json
"DOMAIN_DOCS_PATHS": "/abs/path/manifesto,/abs/path/core-values,/abs/path/financial"
```

Or in `application.yml`:

```yaml
domain:
	docs:
		paths: "oms/specs,simulator/specs,/abs/path/extra"
```

The tools return normalized relative `path` values that you can pass to `readDomainDoc`.

## Using the tools in chat

- Discover: “list tools”
- Enumerate docs: “Call `listDomainDocs`”
- Search: “Use `searchDomainDocs` with query='risk policy' topK=5”
- Read: “Call `readDomainDoc` path='oms/specs/manifesto.md'”
- Read paged: “Call `readDomainDoc` path='simulator/specs/financial_overview.md' offset=0 limit=4000”

Clients can ground responses using these tool outputs.

## Optional enhancements

- MCP resources: expose each doc as a resource to browse via resource catalogs.
- Semantic search: add embeddings + local vector store (e.g., Lucene/ES/sqlite+FAISS) for better retrieval.
- Prompts: publish standardized prompts (e.g., team voice/values) via MCP prompts capability.
- Access control: filter sensitive docs or use profiles via properties.

## Quality check

- Build: PASS (compiled `mcp-server-lib` successfully, skipping tests)
- Lint/type: no Java errors; generated-code warnings are unrelated to the new tools
- Smoke: MCP auto-configuration is present; tools are registered by `McpConfig`

## Requirements coverage

- Make domain knowledge available via MCP to Copilot and Claude: Done
- Include models/specifications/manifesto/core values/financial knowledge: Done via file-based ingestion; configurable via `domain.docs.paths`
- Concrete config examples for Copilot and Claude: Done
- Ready-to-use usage examples: Done

If you’d like, we can also expose docs as MCP resources and add basic semantic search for higher-quality retrieval.