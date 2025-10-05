# MCP Knowledge Server Documentation

This directory contains comprehensive documentation for the MCP (Model Context Protocol) Knowledge Server, which makes OMS specifications available to GitHub Copilot and other AI assistants.

---

## 📚 Documentation Index

### Getting Started
Start here to get up and running quickly:

- **[Quick Start Guide](QUICK_START_GUIDE.md)** ⭐ **Start Here**  
  5-minute guide to get started with spec-driven development using GitHub Copilot

- **[Integration Complete Summary](INTEGRATION_COMPLETE.md)**  
  Overview of what's been set up and how everything works together

### Setup & Configuration

- **[MCP Setup Guide](MCP.md)**  
  How to wire the MCP server to GitHub Copilot (VS Code) and Claude Desktop  
  Includes configuration examples and troubleshooting

### Using the Knowledge Server

- **[Copilot Integration Guide](COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md)**  
  Complete guide on making GitHub Copilot use OMS specs when coding  
  Covers workflows, patterns, and best practices

- **[Prompts Library](COPILOT_PROMPTS_LIBRARY.md)**  
  50+ copy-paste ready prompts for common development tasks:
  - Creating classes from specs
  - Writing tests
  - Code analysis
  - Refactoring
  - Documentation
  - And more...

- **[Quick Reference](QUICK_REFERENCE.md)**  
  Cheat sheet for MCP tools and common usage patterns  
  Handy reference for day-to-day use

### Examples & Demos

- **[Spec-Driven Development Demo](SPEC_DRIVEN_DEMO.md)**  
  Real working example showing how to create an Order entity from specs  
  Demonstrates the complete workflow with actual generated code

- **[Section Navigation Demo](SECTION_NAVIGATION_DEMO.md)**  
  How to use the section-level navigation features  
  Navigate large specs efficiently

### Technical Details

- **[Improvements Summary](IMPROVEMENTS_SUMMARY.md)**  
  Technical details of the section navigation features  
  Benefits, implementation, and comparison

---

## 🎯 Quick Navigation by Task

### I want to...

**Get started quickly**  
→ [Quick Start Guide](QUICK_START_GUIDE.md)

**Learn how to use Copilot with specs**  
→ [Copilot Integration Guide](COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md)

**Find ready-to-use prompts**  
→ [Prompts Library](COPILOT_PROMPTS_LIBRARY.md)

**See a real example**  
→ [Spec-Driven Demo](SPEC_DRIVEN_DEMO.md)

**Configure MCP for my IDE**  
→ [MCP Setup Guide](MCP.md)

**Look up MCP tool syntax**  
→ [Quick Reference](QUICK_REFERENCE.md)

**Understand what was built**  
→ [Integration Complete](INTEGRATION_COMPLETE.md)

---

## 🔧 Available MCP Tools

The knowledge server provides 6 MCP tools accessible via `@workspace` in GitHub Copilot:

1. **`listDomainDocs`** - List all available spec documents
2. **`readDomainDoc`** - Read full documents with pagination
3. **`searchDomainDocs`** - Search across all documents
4. **`listDocSections`** - Get document outline/table of contents
5. **`readDocSection`** - Read specific sections by title
6. **`searchDocSections`** - Search within document sections

See [Quick Reference](QUICK_REFERENCE.md) for usage examples.

---

## 📖 Your Spec Library

The MCP server indexes these OMS specifications:

- **oms_spec.md** - Main OMS State Store specification
- **domain-model_spec.md** - Domain model organization & libraries
- **state-query-store_spec.md** - State store design details
- **streaming_spec.md** - Streaming architecture
- **software-architecture-methodology_spec.md** - Development methodology
- **manifesto.md** - Team manifesto and core values
- **skill_profiles.md** - Skill profiles
- **oms_future_considerations.md** - Future enhancements
- **todo.txt** - TODO items

---

## 💡 The Pattern

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

See [Copilot Integration Guide](COPILOT_KNOWLEDGE_INTEGRATION_GUIDE.md) for details.

---

## 🎓 Learning Path

### Week 1: Learn
1. Read [Quick Start Guide](QUICK_START_GUIDE.md) (5 min)
2. Try the example prompts from [Prompts Library](COPILOT_PROMPTS_LIBRARY.md) (30 min)
3. Review [Spec-Driven Demo](SPEC_DRIVEN_DEMO.md) (15 min)
4. Experiment with your own prompts (ongoing)

### Week 2: Practice
1. Use spec prompts for all new code
2. Add spec references to PRs
3. Review code against specs
4. Build your own prompt templates

### Week 3: Master
1. Teach teammates the pattern
2. Create project-specific workflows
3. Measure spec compliance improvements
4. Share best practices

---

## 🆘 Troubleshooting

**MCP tools not showing in Copilot?**
- Check: `@workspace What MCP tools are available?`
- Should see all 6 tools listed
- If missing: Reload VS Code window

**Empty results from listDomainDocs?**
- Verify configuration in `application.yml`
- Rebuild: `./gradlew bootJar`
- Reload VS Code

**Copilot not using specs?**
- You must explicitly ask with `@workspace`
- Reference specs in your prompts
- Use examples from the Prompts Library

See [MCP Setup Guide](MCP.md) for detailed troubleshooting.

---

## 📝 Contributing

When adding new documentation:
1. Keep it practical and example-driven
2. Include copy-paste ready prompts
3. Link to related docs
4. Update this index
5. Keep the Quick Start simple for newcomers

---

## 🎉 Ready to Start?

Begin with the [Quick Start Guide](QUICK_START_GUIDE.md) and you'll be writing spec-driven code in 5 minutes!

Your OMS specifications are now **living documentation** that actively helps you code! 🚀
