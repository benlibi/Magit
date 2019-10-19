var GET_REPO_INFO = buildUrlWithContextPath("getRepoInfo");
var GET_BRANCHES = buildUrlWithContextPath("getBranches");
var CREATE_BRANCH = buildUrlWithContextPath("createBranch");
var SHOW_COMMIT = buildUrlWithContextPath("showCommit");
var GET_COMMIT_HISTORY = buildUrlWithContextPath("getCommitHistory");
var GET_WC = buildUrlWithContextPath("getWC");
var DELETE_BLOB = buildUrlWithContextPath("deleteBlob");
var SAVE_BLOB = buildUrlWithContextPath("saveBlob");
var WORKING_DIR_FILES;

function createBranch(branchName) {
    $.ajax({
        url: CREATE_BRANCH,
        data: {branchName: branchName},
        timeout: 2000,
        success: function (r) {
            getBranches();
            $("#new_branch").val("")
        }
    });
}

function showCommit(commitSha1) {
    $.ajax({
        url: SHOW_COMMIT,
        data: {commitSha1: commitSha1.text.split(",")[0]},
        timeout: 2000,
        success: function (commitContent) {
            $.jAlert({
                'title': commitSha1.text.split(",")[0] + " Content",
                'content': commitContent,
                'theme': "blue",
                'closeOnClick': true,
                'backgroundColor': 'white',
                'btns': [
                    {'text': 'OK', 'theme': "blue"}
                ]
            });
        }
    });
}

function getBranches() {
    $.ajax({
        url: GET_BRANCHES,
        timeout: 2000,
        success: function (r) {
            $('#branch-select').empty();
            r.forEach(function (branch) {
                var optionBranch = new Option(branch.replace(" (HEAD)", ""), branch);
                $('#branch-select').append(optionBranch);

                $('#branch-select').change(function (e) {
                    e.stopImmediatePropagation();
                    getCommitHistory($(this).val().replace(" (HEAD)", ""))
                });
            })
        }
    });
}

function getRepoInfo() {
    $.ajax({
        url: GET_REPO_INFO,
        timeout: 2000,
        success: function (r) {
            var heading_text = "Repository: " + r.name;
            if (r.isRemote) {
                heading_text += " Tracking after: " + r.remote_name + " of: " + r.remote_path;
            } else {
                heading_text += " Local Repo"
            }
            $('#repo_heading').text(heading_text);
        }
    });
}

function getCommitHistory(branchName) {
    $.ajax({
        type: "GET",
        data: {branchName: branchName},
        url: GET_COMMIT_HISTORY,
        timeout: 2000,
        success: function (r) {
            $("#commit_history").empty();
            r.forEach(function (commit) {
                $("#commit_history").append('<li style="width:100%;"><a style="width:100%;" onclick="showCommit(this)">' + commit + '</a></li>');
            })
        }
    });
}

function getWC() {
    $.ajax({
        type: "GET",
        url: GET_WC,
        timeout: 2000,
        success: function (r) {
            $("#working_directory").empty();

            WORKING_DIR_FILES = r;
            r.forEach(function (blob) {
                $("#working_directory").append('<li id="' + blob.value.name + '" style="width:100%;"><a style="width:100%;" onclick="changeFileView(this)">' + blob.key + '</a></li>');
            });
        }
    });
}

function changeFileView(blobElement) {
    var blob = WORKING_DIR_FILES.find(function (element) {
        return element.key === blobElement.text;
    });

    $.jAlert({
            'type': 'confirm',

            'confirmQuestion': '<textarea id="save_file_box" type="text" style="width: 100%; margin-top: 0; margin-bottom: 0; height: 350px; text-align: left;">' +
                blob.value.content.toString().replace("/n", "<br>") + '</textarea>',

            'theme': 'red',

            'onConfirm': function () {

                theme = "green";
                saveBlob(blob.key, $('#save_file_box').val())
                $.jAlert({

                    'title': 'Save Changes!',

                    'content': 'File As Been Saved !',

                    'theme': theme,

                    'closeOnClick': true,

                    'backgroundColor': 'black'

                });

            },

            'onDeny': function () {

                $.jAlert({
                    'type': 'confirm',

                    'confirmQuestion': 'Do You Want To Delete This File ?',

                    'theme': 'red',

                    'onConfirm': function () {

                        theme = "red";

                        deleteBlob(blob.key);

                        $.jAlert({

                            'title': 'File(s) Removed',

                            'content': 'The files are removed!',

                            'theme': theme,

                            'closeOnClick': true,

                            'backgroundColor': 'black'

                        });

                    },

                    'onDeny': function () {

                        theme = "green";

                        $.jAlert({

                            'title': 'Action Denied',

                            'content': 'File is not removed! Close the dialog by clicking the OK button',

                            'theme': theme,

                            'backgroundColor': 'white',

                            'btns': [

                                {'text': 'OK', 'theme': theme}

                            ]
                        });

                    }
                });
            },

            'confirmBtnText': "Save Changes!",

            'denyBtnText': 'Cancel'
        }
    );
}

function deleteBlob(blobPath) {
    $.ajax({
        url: DELETE_BLOB,
        data: {blobPath: blobPath},
        timeout: 2000,
        success: function (r) {
            getWC();
        }
    });
}

function saveBlob(blobPath, content) {
    $.ajax({
        url: SAVE_BLOB,
        data: {blobPath: blobPath, blobContent: content},
        timeout: 2000,
        success: function (r) {
            getWC();
        }
    });
}

$(function () { // onload...init page
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
    getCommitHistory("");
    getWC();
});