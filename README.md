# Continuous integration & pull requests
- Template already has a few GitHub Actions workflows included. Please ensure you're passing the checks locally, before opening pull request. To do that, either run commands in the IDE terminal, or setup a github hook. Commands are: `./gradlew ktlintFormat`, `./gradlew detektDebug`. **Request a review only after the CI checks have passed successfully.** <br />
- If pull request contains code that should close the issue, please write: close #1, close #2(number == issue number) somewhere in the PR description. This allows for automatic issue closing upon successfull PR merge.
- Commit code as many times as you want while working on a feature. When the feature is ready - do a careful rebase over origin/master and squash all this stuff into one or two meaningful commits that clearly represent the feature, before opening a pull request.
- Features should be splitted into logical chunks if they require a lot of code changes.
- Attempt to keep PR size in range of 250 - 300 lines of code changed.
