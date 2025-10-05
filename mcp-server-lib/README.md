# Spring AI MCP Server Library

A **Model Context Protocol (MCP)** server bu### Key Classes

| Class | Purpose |
|-------|---------|
| `SpringAiApplication` | Main Spring Boot application entry point |
| `OrderSearchMcpTools` | MCP tool provider with `@Tool` annotated methods |
| `DomainDocsTools` | Knowledge server tools for accessing OMS specs |
| `OrderQueryClient` | REST client for OMS API calls with logging |
| `DemoOrderController` | Optional REST endpoint for local testing |
| `McpConfig` | Explicit ToolCallbackProvider bean configuration |
| `HealthTools` | Simple ping/pong tool for connectivity testing |pring Boot and Spring AI, exposing OMS (Order Management System) query capabilities as MCP tools for AI assistants and clients.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [Configuration](#configuration)
6. [Available Tools](#available-tools)
7. [Domain Knowledge Server](#domain-knowledge-server)
8. [Client Integration](#client-integration)
9. [Development](#development)
10. [Logging & Debugging](#logging--debugging)
11. [Troubleshooting](#troubleshooting)
12. [Documentation](#documentation)

---

## Overview

This project implements an **MCP server** that:
- Runs over **stdio** (standard input/output) for seamless integration with MCP clients (e.g., VS Code, Claude Desktop)
- Exposes **OMS order search** functionality via the `searchOrders` tool
- Provides **domain knowledge access** through 6 specialized tools for reading and searching OMS specifications
- Uses **Spring AI's MCP server autoconfiguration** for automatic tool discovery
- Supports **typed filters, pagination, and sorting** for flexible order queries
- Provides a **REST client** (`OrderQueryClient`) that handles multiple response formats (Spring Data REST, HAL, plain JSON arrays)

**Key Features:**
- Zero-boilerplate tool registration using Spring AI `@Tool` annotations
- **Domain knowledge server** making OMS specs accessible to AI assistants
- **Section-level navigation** for precise spec access
- Lombok-based logging with detailed request/response tracing
- OpenAPI-generated models for type-safe filter definitions
- Dual-mode operation: MCP server (stdio) and optional REST API demo endpoint

---

## Architecture

### Components

```mermaid
%%{init: {'theme':'base', 'themeVariables': { 'primaryColor':'#e3f2fd','primaryTextColor':'#1565c0','primaryBorderColor':'#1976d2','lineColor':'#42a5f5','secondaryColor':'#fff3e0','tertiaryColor':'#e8f5e9'}}}%%
flowchart LR
    Client["MCP Client"]
    
    subgraph Server["Spring AI MCP Server"]
        direction TB
        AutoConfig["McpServerAutoConfiguration"]
        Tools["OrderSearchMcpTools"]
        QueryClient["OrderQueryClient"]
        
        AutoConfig -.->|registers| Tools
        Tools -->|uses| QueryClient
    end
    
    OMS["OMS Backend"]
    
    Client ==>|stdio JSON-RPC| AutoConfig
    QueryClient ==>|HTTP GET| OMS
    
    classDef clientStyle fill:#e3f2fd,stroke:#1976d2,stroke-width:2px,color:#000
    classDef serverStyle fill:#fff3e0,stroke:#f57c00,stroke-width:2px,color:#000
    classDef omsStyle fill:#e8f5e9,stroke:#388e3c,stroke-width:2px,color:#000
    
    class Client clientStyle
    class AutoConfig,Tools,QueryClient serverStyle
    class OMS omsStyle
```

### Key Classes

| Class | Purpose |
|-------|---------|
| `SpringAiApplication` | Main Spring Boot application entry point |
| `OrderSearchMcpTools` | MCP tool provider with `@Tool` annotated methods |
| `OrderQueryClient` | REST client for OMS API calls with response parsing |
| `DemoOrderController` | Optional REST endpoint for local testing |
| `McpConfig` | Explicit ToolCallbackProvider bean configuration |
| `HealthTools` | Simple ping/pong tool for connectivity testing |

---

## Prerequisites

- **Java 21** or higher
- **Gradle 8+** (wrapper included)
- **PowerShell** (Windows) or **Bash** (Linux/Mac)
- **OMS Backend** running on `http://localhost:8090` (configurable)
- **MCP Client** (e.g., VS Code with MCP extension, Claude Desktop)

---

## Quick Start

### 1. Build and Run

**Windows (PowerShell):**
```powershell
.\run-mcp.ps1
```

**Linux/Mac (Bash):**
```bash
./run-mcp.sh
```

These scripts:
1. Build the project with `./gradlew bootJar -x test` (skip tests for speed)
2. Locate the generated JAR in `build/libs/`
3. Launch with `--mcp.transport=stdio` and `--spring.profiles.active=mcp`

### 2. Verify Connection

The server will:
- Start in **stdio mode** (no web server, no console logging to keep stdio clean)
- Log to `logs/spring-ai.log`
- Register tools automatically (should see "Registered tools: X" in logs)

### 3. Test with MCP Client

If using **VS Code**, the `.vscode/mcp.json` configuration should auto-connect:

```jsonc
{
  "servers": {
    "oms-order-query": {
      "type": "stdio",
      "command": "powershell.exe",
      "args": ["-ExecutionPolicy", "Bypass", "-File", "${workspaceFolder}\\run-mcp.ps1"],
      "env": {
        "SPRING_PROFILES_ACTIVE": "mcp",
        "MCP_TRANSPORT": "stdio"
      }
    }
  }
}
```

---

## Configuration

### Application Profiles

- **`mcp` profile** (default): Runs in MCP mode with stdio transport and file logging
- **`local` profile**: Optional local development with additional debug logging

### Key Properties (`application.yml`)

```yaml
spring:
  application:
    name: spring-ai
  main:
    web-application-type: none  # Disable web server for stdio mode
    banner-mode: off

oms:
  base-url: http://localhost:8090  # OMS backend URL

mcp:
  transport: stdio  # or "sse" for Server-Sent Events (if supported)

logging:
  level:
    org.springframework.ai.mcp: DEBUG
    org.example.spring_ai.oms: INFO
```

### Environment Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `MCP_TRANSPORT` | Transport mode (`stdio` or `sse`) | `stdio` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `mcp` |
| `JAVA_OPTS` | JVM arguments | - |

**Example:**
```powershell
$env:MCP_TRANSPORT = "stdio"
$env:SPRING_PROFILES_ACTIVE = "mcp"
.\run-mcp.ps1
```

---

## Available Tools

### 1. `searchOrders`

Search OMS orders with typed filters, pagination, and sorting.

**Tool Signature:**
```java
@Tool(name = "searchOrders", 
      description = "Search OMS orders with typed filters, pagination and sorting.")
public OrderSearchResponse searchOrders(
    OrderSearchFilters filters,  // Optional typed filters
    Integer page,                // 0-based page index
    Integer size,                // Page size (default: 20)
    String sort                  // Sort spec: "field,DESC;field2,ASC"
)
```

**Filter Fields (`OrderSearchFilters`):**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `orderId` | String | Exact order ID match | `"01K6PVA884EMR9C4ZC4FTSWKBH"` |
| `orderIdLike` | String | Order ID pattern match | `"01K6%"` |
| `symbol` | String | Security symbol | `"INTC"` |
| `symbolLike` | String | Symbol pattern match | `"INT%"` |
| `account` | String | Account identifier | `"ACC123"` |
| `side` | Enum | Order side | `BUY`, `SELL`, `SELL_SHORT` |
| `ordType` | Enum | Order type | `LIMIT`, `MARKET`, `STOP`, `STOP_LIMIT` |
| `state` | Enum | Order state | `NEW`, `LIVE`, `FILLED`, `CXL`, `REJ` |
| `cancelState` | Enum | Cancel state | `CXL`, `PCXL`, `PMOD`, `REJ` |
| `price` | String | Exact price | `"22.35"` |
| `priceGt` | String | Price greater than | `"20.00"` |
| `priceGte` | String | Price ≥ | `"20.00"` |
| `priceLt` | String | Price less than | `"25.00"` |
| `priceLte` | String | Price ≤ | `"25.00"` |
| `priceBetween` | String | Price range | `"20.00,25.00"` |
| `orderQtyBetween` | String | Quantity range | `"100,500"` |
| `orderQtyGt` | String | Quantity > | `"100"` |
| `orderQtyLt` | String | Quantity < | `"1000"` |
| `transactTimeBetween` | String | Transaction time range | `"2025-10-01T00:00:00,2025-10-31T23:59:59"` |
| `sendingTimeBetween` | String | Sending time range | `"2025-10-01T00:00:00,2025-10-31T23:59:59"` |
| `expireTimeBetween` | String | Expiration time range | `"2025-10-01T00:00:00,2025-10-31T23:59:59"` |

**Response Format:**
```json
{
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1,
  "content": [
    {
      "orderId": "01K6PVA884EMR9C4ZC4FTSWKBH",
      "symbol": "INTC",
      "side": "BUY",
      "state": "UNACK",
      "ordType": "LIMIT",
      "price": 22.35,
      "orderQty": 200.0,
      "transactTime": "2025-10-04T10:01:00"
    }
  ]
}
```

**Example Queries (MCP Client):**

```javascript
// Search for all BUY orders
searchOrders({ side: "BUY" }, 0, 20, "transactTime,desc")

// Search for INTC orders with price > 20
searchOrders({ 
  symbol: "INTC", 
  priceGt: "20.00" 
}, 0, 20, null)

// Search orders in a time range
searchOrders({
  transactTimeBetween: "2025-10-01T00:00:00,2025-10-31T23:59:59"
}, 0, 50, "transactTime,desc")
```

### 2. `ping`

Health check tool to verify MCP server connectivity and tool discovery.

**Tool Signature:**
```java
@Tool(name = "ping", 
      description = "Health check tool to verify MCP tool discovery.")
public String ping()
```

**Response:**
```
"pong"
```

### 3. Domain Knowledge Tools

The server exposes **6 additional tools** for accessing OMS specifications and domain knowledge. These tools enable AI assistants like GitHub Copilot to read and search your documentation.

| Tool | Description |
|------|-------------|
| `listDomainDocs` | List all available spec documents with metadata |
| `readDomainDoc` | Read full document content with pagination support |
| `searchDomainDocs` | Keyword search across all documents |
| `listDocSections` | Get document outline (table of contents) |
| `readDocSection` | Read specific section by title |
| `searchDocSections` | Search within document sections for precision |

**See [Domain Knowledge Server](#domain-knowledge-server) for details.**

---

## Domain Knowledge Server

The MCP server includes a **knowledge server** that makes your OMS specifications accessible to AI assistants, enabling spec-driven development workflows.

### What It Does

- **Indexes OMS specs** from `oms/specs/` directory
- **Exposes 6 MCP tools** for discovery, reading, and searching
- **Enables GitHub Copilot** to use specs when generating code, tests, and documentation
- **Provides section-level navigation** for large specification documents

### Indexed Specifications

The server automatically indexes these documents:

- `oms_spec.md` - Main OMS State Store specification (10 KB)
- `domain-model_spec.md` - Domain model organization & libraries (4.4 KB)
- `state-query-store_spec.md` - State store design details (10.7 KB)
- `streaming_spec.md` - Streaming architecture (13.8 KB)
- `software-architecture-methodology_spec.md` - Development methodology (10.1 KB)
- `manifesto.md` - Team manifesto and core values (7.4 KB)
- `skill_profiles.md` - Skill profiles (10.9 KB)
- `oms_future_considerations.md` - Future enhancements (18.3 KB)
- `todo.txt` - TODO items (311 B)

### Configuration

**Default location:** `/home/tbaderts/data/workspace/oms/specs`

To change or add spec directories, update `application.yml`:

```yaml
domain:
  docs:
    paths: /home/tbaderts/data/workspace/oms/specs,/path/to/more/specs
```

### Using with GitHub Copilot

Once configured, you can ask Copilot to use your specs:

```
@workspace Search specs for "state machine" and explain what you find

@workspace Using the OMS spec, create the Order entity class with spec references

@workspace Generate tests for Order state transitions based on the State Machine spec

@workspace Compare OrderService.java against the CQRS architecture from the spec
```

### Example Tool Calls

**List all specs:**
```javascript
listDomainDocs()
// Returns: Array of {path, name, size, lastModified}
```

**Read a section:**
```javascript
readDocSection({
  path: "specs/oms_spec.md",
  sectionTitle: "Domain Model"
})
// Returns: Just that section and its subsections
```

**Search for concepts:**
```javascript
searchDocSections({
  query: "validation rules",
  topK: 5
})
// Returns: Top 5 matching sections with context
```

### Benefits

- ✅ **Spec-driven development** - Code follows specifications by default
- ✅ **Faster onboarding** - New developers can query specs via AI
- ✅ **Consistent codebase** - All code follows same spec patterns
- ✅ **Better code reviews** - Validate against specs automatically
- ✅ **Living documentation** - Specs actively used in development

### Learn More

See the comprehensive documentation in [`docs/`](docs/):

- **[Quick Start Guide](docs/QUICK_START_GUIDE.md)** - Get started in 5 minutes
- **[Copilot Integration Guide](docs/COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md)** - Complete usage guide
- **[Prompts Library](docs/COPILOT_PROMPTS_LIBRARY.md)** - 50+ ready-to-use prompts
- **[MCP Setup](docs/MCP.md)** - Configuration and troubleshooting

---

## Client Integration

### VS Code MCP Extension

1. Create `.vscode/mcp.json` in your workspace:
```jsonc
{
  "servers": {
    "oms-order-query": {
      "type": "stdio",
      "command": "powershell.exe",
      "args": [
        "-ExecutionPolicy", "Bypass",
        "-File", "${workspaceFolder}\\run-mcp.ps1"
      ],
      "env": {
        "SPRING_PROFILES_ACTIVE": "mcp",
        "MCP_TRANSPORT": "stdio"
      }
    }
  }
}
```

2. Reload VS Code
3. The MCP server will auto-start and register tools
4. Use Copilot Chat with tool references: `#mcp_oms-order-que_searchOrders`

### Claude Desktop

Add to `claude_desktop_config.json`:
```json
{
  "mcpServers": {
    "oms-order-query": {
      "command": "powershell.exe",
      "args": [
        "-ExecutionPolicy", "Bypass",
        "-File", "C:\\path\\to\\mcp-server-lib\\run-mcp.ps1"
      ]
    }
  }
}
```

### Programmatic MCP Client (JavaScript)

```javascript
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

const transport = new StdioClientTransport({
  command: "powershell.exe",
  args: ["-ExecutionPolicy", "Bypass", "-File", "run-mcp.ps1"]
});

const client = new Client({
  name: "oms-client",
  version: "1.0.0"
}, {
  capabilities: {}
});

await client.connect(transport);

// Call searchOrders tool
const result = await client.callTool({
  name: "searchOrders",
  arguments: {
    filters: { side: "BUY" },
    page: 0,
    size: 20,
    sort: "transactTime,desc"
  }
});

console.log(result.content);
```

---

## Development

### Project Structure

```
mcp-server-lib/
├── docs/                                  # Comprehensive documentation
│   ├── README.md                          # Documentation index
│   ├── QUICK_START_GUIDE.md               # 5-minute getting started
│   ├── COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md  # Complete usage guide
│   ├── COPILOT_PROMPTS_LIBRARY.md         # 50+ ready-to-use prompts
│   ├── SPEC_DRIVEN_DEMO.md                # Real working examples
│   ├── QUICK_REFERENCE.md                 # MCP tools cheat sheet
│   ├── MCP.md                             # MCP setup and configuration
│   ├── SECTION_NAVIGATION_DEMO.md         # Section navigation guide
│   ├── IMPROVEMENTS_SUMMARY.md            # Technical details
│   └── INTEGRATION_COMPLETE.md            # Setup summary
├── src/main/java/org/example/
│   ├── spring_ai/
│   │   ├── SpringAiApplication.java       # Main entry point
│   │   ├── docs/
│   │   │   └── DomainDocsTools.java       # Knowledge server tools
│   │   ├── oms/
│   │   │   ├── OrderSearchMcpTools.java   # MCP tool provider
│   │   │   ├── OrderQueryClient.java      # REST client with logging
│   │   │   ├── DemoOrderController.java   # Optional REST demo
│   │   │   ├── McpConfig.java             # Tool callback config
│   │   │   ├── PageResponse.java          # Pagination wrapper
│   │   │   └── LoggingInterceptor.java    # HTTP request/response logging
│   │   └── tools/
│   │       └── HealthTools.java           # Ping tool
│   └── common/                            # OpenAPI-generated models
│       ├── api/
│       └── model/query/
├── src/main/openapi/
│   ├── oms-query-api.yml                  # OpenAPI spec for code generation
│   └── oms-cmd-api.yml
├── src/main/resources/
│   ├── application.yml                    # Main config
│   ├── application-local.yml              # Local profile overrides
│   └── logback-spring.xml                 # Logging config (file only, no console)
├── .vscode/
│   └── mcp.json                           # VS Code MCP client config
├── build.gradle                           # Gradle build with OpenAPI plugin
├── run-mcp.ps1                            # PowerShell launcher
├── run-mcp.sh                             # Bash launcher
└── README.md                              # This file
```

### Adding New Tools

1. Create a new `@Component` class
2. Add `@Tool` annotated methods:
```java
@Component
public class MyTools {
    @Tool(name = "myTool", description = "Does something useful")
    public String myTool(String param) {
        return "result";
    }
}
```
3. Spring AI will auto-discover and register the tool
4. Rebuild and restart the MCP server

### Building

**Full build with tests:**
```powershell
.\gradlew clean build
```

**Fast build (skip tests):**
```powershell
.\gradlew bootJar -x test
```

**Run locally (REST mode for testing):**
```powershell
.\gradlew bootRun --args='--spring.main.web-application-type=servlet --server.port=8091'
```

Then test the demo endpoint:
```powershell
Invoke-RestMethod -Uri 'http://localhost:8091/api/demo/orders' -Method GET
```

### Testing Tools

**Unit test example:**
```java
@SpringBootTest
class OrderSearchMcpToolsTest {
    @Autowired
    private OrderSearchMcpTools tools;
    
    @Test
    void testSearchOrders() {
        var response = tools.searchOrders(
            new OrderSearchFilters(/* ... */),
            0, 20, null
        );
        assertThat(response.content()).isNotEmpty();
    }
}
```

---

## Logging & Debugging

### Log Files

All logs go to **`logs/spring-ai.log`** (no console output to keep stdio clean).

**Log levels (`logback-spring.xml`):**
```xml
<logger name="org.springframework.ai.mcp" level="DEBUG"/>
<logger name="org.example.spring_ai.oms" level="INFO"/>
<logger name="org.example.spring_ai.tools" level="DEBUG"/>
```

### Request/Response Logging

`OrderQueryClient` logs:
- **Request URI** with all query parameters
- **Raw response body** from OMS backend

Example log output:
```
INFO  OrderQueryClient - OMS Order Search Request - URI: /api/query/orders?side=BUY&page=0&sort=transactTime,desc&size=20
INFO  LoggingInterceptor - Request: GET http://localhost:8090/api/query/orders?side=BUY&page=0&sort=transactTime,desc&size=20
INFO  LoggingInterceptor - Response: 200 OK
INFO  OrderQueryClient - OMS Order Search Response - Body: {"content":[...],"page":{...}}
```

### MCP Protocol Debugging

Set environment variable for detailed MCP protocol tracing:
```powershell
$env:LOGGING_LEVEL_IO_MODELCONTEXTPROTOCOL = "TRACE"
.\run-mcp.ps1
```

---

## Troubleshooting

### Issue: "content size is 0 but logs show data"

**Cause:** Response format mismatch. The `OrderQueryClient` expects one of:
1. `{"content": [...], "page": {...}}` (Spring Data REST)
2. `{"_embedded": {"orders": [...]}, "page": {...}}` (HAL)
3. `[...]` (plain array)

**Fix:** Check `OrderQueryClient.search()` parsing logic matches your OMS response format.

### Issue: "Build failed" during startup

**Cause:** Gradle wrapper issues or missing Java 21.

**Fix:**
```powershell
# Verify Java version
java -version  # Should be 21+

# Re-download Gradle wrapper
.\gradlew wrapper --gradle-version 8.11
```

### Issue: "Connection refused to localhost:8090"

**Cause:** OMS backend not running or wrong port.

**Fix:**
1. Start OMS backend on port 8090
2. Or update `oms.base-url` in `application.yml`

### Issue: "No tools registered"

**Cause:** Spring component scanning not finding `@Tool` classes.

**Fix:**
1. Verify `@Component` annotation on tool classes
2. Check package structure (should be under `org.example.spring_ai`)
3. Add explicit scan: `@ComponentScan("org.example.spring_ai")` to main class

### Issue: "Protocol version mismatch"

**Symptom:** Warning: `Client requested unsupported protocol version: 2025-06-18`

**Fix:** This is informational only. The server will negotiate down to `2024-11-05` (supported by Spring AI 1.0.2).

### Issue: "Logs not appearing"

**Cause:** Logback not configured for file output.

**Fix:** Verify `logback-spring.xml` has:
```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/spring-ai.log</file>
    ...
</appender>
```

---

## Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory.

### 📚 Complete Documentation Index

**[Browse all documentation →](docs/README.md)**

### Quick Links by Topic

#### Getting Started
- **[Quick Start Guide](docs/QUICK_START_GUIDE.md)** ⭐ - Get started in 5 minutes
- **[Integration Complete](docs/INTEGRATION_COMPLETE.md)** - Overview of the complete setup

#### Configuration & Setup
- **[MCP Setup Guide](docs/MCP.md)** - Wire to GitHub Copilot and Claude Desktop

#### Using the Knowledge Server
- **[Copilot Integration Guide](docs/COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md)** - Make Copilot use OMS specs
- **[Prompts Library](docs/COPILOT_PROMPTS_LIBRARY.md)** - 50+ copy-paste ready prompts
- **[Quick Reference](docs/QUICK_REFERENCE.md)** - MCP tools cheat sheet

#### Examples & Tutorials
- **[Spec-Driven Development Demo](docs/SPEC_DRIVEN_DEMO.md)** - Real working example
- **[Section Navigation Demo](docs/SECTION_NAVIGATION_DEMO.md)** - Navigate large specs efficiently

#### Technical Details
- **[Improvements Summary](docs/IMPROVEMENTS_SUMMARY.md)** - Section navigation features

### 🎯 Common Tasks

| I want to... | See this document |
|--------------|-------------------|
| Get started quickly | [Quick Start Guide](docs/QUICK_START_GUIDE.md) |
| Configure MCP for my IDE | [MCP Setup](docs/MCP.md) |
| Use Copilot with specs | [Copilot Integration Guide](docs/COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md) |
| Find ready-to-use prompts | [Prompts Library](docs/COPILOT_PROMPTS_LIBRARY.md) |
| See a real example | [Spec-Driven Demo](docs/SPEC_DRIVEN_DEMO.md) |
| Look up MCP tool syntax | [Quick Reference](docs/QUICK_REFERENCE.md) |

### 💡 The Pattern for Spec-Driven Development

Every time you code with Copilot:

1. **Ask Copilot to read specs first:**
   ```
   @workspace Search specs for "[YOUR TOPIC]"
   ```

2. **Then generate/analyze code:**
   ```
   @workspace Based on the spec, generate [CODE]
   ```

3. **Always validate:**
   ```
   @workspace Does this match spec requirements?
   ```

See the [Copilot Integration Guide](docs/COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md) for complete details and examples.

---

## Additional Resources

- **Spring AI Docs:** https://docs.spring.io/spring-ai/reference/
- **Model Context Protocol Spec:** https://spec.modelcontextprotocol.io/
- **MCP SDK (TypeScript):** https://github.com/modelcontextprotocol/typescript-sdk
- **OpenAPI Generator:** https://openapi-generator.tech/

---

## License

This project is for internal use and demonstration purposes. Adjust licensing as needed for your organization.

---

**Maintained by:** [Your Team]  
**Last Updated:** October 4, 2025
