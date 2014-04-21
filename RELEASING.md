# Releasing a new version of SockJS Servlet

### Prerequisites

Make sure you have an account at https://oss.sonatype.org and that
you're a member of the org.projectodd group.

### Verify Examples Work

Take a minute to go through each example in the `examples/` directory
and make sure they all build and work with the latest version.

### Update Version Numbers

    $ support/release.sh NEW_VERSION NEXT_VERSION

### Verify Artifacts

Look for our staging repository at
https://oss.sonatype.org/#stagingRepositories - the name will start
with "orgprojectodd". Click the repository's "Content" tab and make
sure the artifacts and versions listed look correct.

### Release the Staging Deployment

http://central.sonatype.org/pages/releasing-the-deployment.html

From the staging repository list, highlight our repository and click
the "Close" button at the top of the grid followed by the "Release"
button. If the close process fails, you'll want to figure out why,
"Drop" instead of "Release" this repository, and start again.

### Push Changes to GitHub

    git push --tags origin master