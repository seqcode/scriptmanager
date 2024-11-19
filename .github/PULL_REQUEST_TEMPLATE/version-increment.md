---
name: Version Increment
about: Use this template's checklist to version increment

---

The [Release Roadmap](https://github.com/orgs/CEGRcode/projects/9) on Github organizes issue tickets and creates a projection of which issues should be addressed for each release. This helps when writing up the release notes/change log, tagging all the appropriate issues, as well as visually tracking what tasks are left to do in each release. When we are ready for a release, the following checklist should be followed to ensure that we update everything together without missing anything.

### Check [Release Roadmap]((https://github.com/orgs/CEGRcode/projects/9))
- [ ] Make sure all issues are closed and pulled into dev
- [ ] Update roadmap so next version is first to display

### [Docusaurus updates](https://github.com/CEGRcode/scriptmanager-docs)
- [ ] Make sure new tools have their own pages that thoroughly describe what they do
- [ ] Affected tools have been updated accordingly (check logs for list of tools)
- [ ] Make sure last updated timestamps are appropriate/correct
- [ ] Increment version across docs
- [ ] Update Download links for latest version on the Getting Started page

### Testing
- [ ] Ideally some degree of user testing on the development version has been performed (ask the bench scientists).
- [ ] Make sure latest [Github Actions build](https://github.com/CEGRcode/scriptmanager/actions/workflows/gradle.yml) ran successfully
- [ ] Any CI tests that exist should be run (`gradlew test`)

### Release commit
- [ ] `build.gradle`
  - [ ] Increment version (`version = ____`) and strip dev from JAR filename
- [ ] `src/objects/ToolDescriptions.java`
  - [ ] Increment ScriptManager version constant (used by all CLI tools and GUI JFrame title for versioning)

### Github Release/version tag
- [ ] Commit & pull request, review into master
- [ ] Create version release & add version tag to the commit id
- [ ] Compile JAR and save with source tar archive on release page
- [ ] Write up summary for the version tag commit including change log that links to resolved/relevant issue tickets (refer to Release Roadmap)

### Switch naming back to dev
- [ ] `build.gradle` file should switch naming JAR to use dev
- [ ] `src/objects/ToolDescriptions.java` file should switch naming JAR to use dev
