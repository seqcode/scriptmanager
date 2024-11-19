---
name: New Tool
about: Use this template's checklist for brand new tools
title: ''
labels: New Tool
assignees: ''

---

## Describe tool

... Blah blah blah blah...

## Checklist

New tools should be written on branches off `main`. A pull request to the `main` branch can then be submitted and a reviewer will review the code and accept the merge.

- [ ] Write **tests** for Github Actions (automatic testing)
- [ ] Write tool (list changed files)
  - [ ] `objects.ToolDescriptions.java`
  - [ ] `scripts.Tool_Group.MyTool.java`
  - [ ] `cli.Tool_Group.MyToolCLI.java`
  - [ ] `window_interface.Tool_Group.MyToolWindow.java`
  - [ ] `window_interface.Tool_Group.MyToolOutput.java` (Optional)
  - [ ] `main.ScriptManagerGUI.java`
  - [ ] `main.ScriptManager.java`
- [ ] Add logging support
- [ ] Update documentation (docusaurus)
- [ ] Write **Galaxy wrapper**

Then you can pull your changes into master! 🎉

### Developer tip: Copy-paste source code from a similar tool in scriptmanager for the implementation
