var GET_REPO_INFO = buildUrlWithContextPath("getRepoInfo");
var GET_BRANCHES = buildUrlWithContextPath("getBranches");
var GET_COMMIT_HISTORY = buildUrlWithContextPath("getCommitHistory");

function getBranches(){
    $.ajax({
        url: GET_BRANCHES,
        timeout: 2000,
        success: function(r) {
            r.forEach(function (branch) {
                $('#branch-select').append(new Option(branch.replace(" (HEAD)",""), branch));

                // if(branch.includes(" (HEAD)")){
                //     $('#branch-select').value(branch.replace(" (HEAD)",""));
                // }
            })
        }
    });
}

function getRepoInfo(){
    $.ajax({
        url: GET_REPO_INFO,
        timeout: 2000,
        success: function(r) {
            var heading_text = "Repository: " + r.name;
            if(r.isRemote){
                heading_text += " Tracking after: " + r.remote_name + " of: " + r.remote_path;
            }else{
                heading_text += " Local Repo"
            }
            $('#repo_heading').text(heading_text);
        }
    });
}

function getCommitHistory(){
    $.ajax({
        url: GET_COMMIT_HISTORY,
        timeout: 2000,
        success: function(r) {
            r.forEach(function (commit) {
                $("#commit-history").append('<li style="width:100%;"><a style="width:100%;" href="#daniel">' + commit + '</a></li>');
            })
        }
    });
}


$(function() { // onload...init page
    // $('#side-menu').append(
    // '<ul>' +
    // '<li><a class="active" href="user.html">Home</a></li>' +
    // '<li><a href="branches.html">Branches</a></li>' +
    // '<li><a href="commits.html">Commits</a></li>' +
    // '<li><a href="wc.html">Working Copy</a></li>' +
    // '<li><a href="merge.html">Merge</a></li>' +
    // '<li><a href="remote.html">Remote</a></li>' +
    // '<li><a href="pr.html">Pull Request</a></li>' +
    // '</ul>'
    // );
    getBranches();
    getRepoInfo();
    getCommitHistory();
});