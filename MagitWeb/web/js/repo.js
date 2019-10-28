var GET_REPO_INFO = buildUrlWithContextPath("getRepoInfo");
var GET_BRANCHES = buildUrlWithContextPath("getBranches");
var GET_REMOTE_BRANCHES = buildUrlWithContextPath("getRemoteBranches");
var CREATE_BRANCH = buildUrlWithContextPath("createBranch");
var SHOW_COMMIT = buildUrlWithContextPath("showCommit");
var GET_COMMIT_HISTORY = buildUrlWithContextPath("getCommitHistory");
var GET_WORKING_DIRECTORY = buildUrlWithContextPath("getWorkingDirectory");
var GET_WC = buildUrlWithContextPath("getWC");
var DELETE_BLOB = buildUrlWithContextPath("deleteBlob");
var SAVE_BLOB = buildUrlWithContextPath("saveBlob");
var PULL = buildUrlWithContextPath("pull");
var PUSH = buildUrlWithContextPath("push");
var CREATE_NEW_FILE = buildUrlWithContextPath("createFile");
var COMMIT = buildUrlWithContextPath("commit");
var WORKING_DIR_FILES;
var GET_USER_MSG = buildUrlWithContextPath("getMsg");
var CREATE_PR = buildUrlWithContextPath("createPr");
var GET_PR = buildUrlWithContextPath("getPr");
var CANCEL_PR = buildUrlWithContextPath("cancelPr");
var APPROVE_PR = buildUrlWithContextPath("approvePr");
var repoPrs;

function createBranch(branchName) {
    $.ajax({
        url: CREATE_BRANCH,
        data: {branchName: branchName},
        timeout: 2000,
        success: function (r) {
            getBranches();
            $("#new_branch").val("");
            getWC();
            initWorkingDirectory();
        }
    });
}


function getUserMsg(){
    $.ajax({
        url: GET_USER_MSG,
        timeout: 2000,
        success: function(r) {
            var usr_mag = "";
            r.forEach(function(msg){
                usr_mag += msg + "\n";
            });
            if(usr_mag===""){
                usr_mag = "No messages..."
            }
            document.getElementById("usr_msg_textbox_repo").value=usr_mag;
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
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown',
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

                if (branch.includes('(HEAD)')) {
                    var headBranchFixedName = branch.replace(" (HEAD)", "");
                    var optionBranch = new Option(headBranchFixedName, branch);
                    $('#branch-select').append(optionBranch);
                    $('#branch-select').val(branch);

                    renderNewContent(headBranchFixedName);
                } else {
                    var optionBranch = new Option(branch, branch);
                    $('#branch-select').append(optionBranch);
                }
            })
        }
    });

    $('#branch-select').change(function (e) {
        e.stopImmediatePropagation();
        renderNewContent($(this).val().replace(" (HEAD)", ""));
    });
}

function renderNewContent(branchName) {
    $.ajax({
        type: "GET",
        data: {branchName: branchName},
        url: GET_COMMIT_HISTORY,
        timeout: 2000,
        success: function (r) {
            $("#commit_history").empty();
            r.forEach(function (commit) {
                $("#commit_history").append('<li style="width:100%;"><a style="width:100%;" onclick="showCommit(this)">' + commit + '</a></li>');
            });

            // getWC();
            // initWorkingDirectory();
        },
        complete: function () {
            getWC();
            initWorkingDirectory();
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
                heading_text += " Tracking after: " + r.remote_name + " of: " + r.remote_user;
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

function initWorkingDirectory() {
    $.ajax({
        type: "GET",
        url: GET_WORKING_DIRECTORY,
        data: {init: true},
        timeout: 2000,
        success: function (r) {
            WORKING_DIR_FILES = r;
        },
        complete: function () {
            getWorkingDirectory();
        }
    });
}

function getWorkingDirectory() {
    $.ajax({
        type: "GET",
        url: GET_WORKING_DIRECTORY,
        timeout: 2000,
        success: function (dirMap) {
            $("#working_directory").empty();
            var nestedDirTreeView = "";

            var origin = dirMap.find(function (element) {
                return element.key.includes("(HEAD)");
            });
            nestedDirTreeView = f(origin, dirMap, nestedDirTreeView, 10);

            $("#working_directory").append(nestedDirTreeView);
        },
        complete: function () {
            var toggler = document.getElementsByClassName("caret");
            var i;

            for (i = 0; i < toggler.length; i++) {
                toggler[i].addEventListener("click", function () {
                    this.parentElement.querySelector(".nested").classList.toggle("active");
                    this.classList.toggle("caret-down");
                });
            }
        }
    });
}

function f(file, map, nestedDirTreeView, marginLeft) {
    nestedDirTreeView = nestedDirTreeView.concat('<li><span class="caret" style="display: block;margin-left: ' + marginLeft + 'px' + ';">' + file.key.replace(" (HEAD)", "") + '</span>');
    nestedDirTreeView = nestedDirTreeView.concat('<ul class="nested" style="width: 100%; overflow: unset;">');

    file.value.forEach(function (blob) {
        if (!blob.isDir) {
            nestedDirTreeView = nestedDirTreeView.concat('<li id="' + blob.name + '" style="width:100%; margin-left: 15px;"><a style="width:100%;" onclick="changeFileView(this)">' + blob.path + '</a></li>');
        }
    });

    file.value.forEach(function (blob) {
        if (blob.isDir) {
            var childDir = map.find(function (element) {
                return element.key === blob.name;
            });
            nestedDirTreeView = f(childDir, map, nestedDirTreeView, marginLeft + 10);
        }
    });

    nestedDirTreeView = nestedDirTreeView.concat('</ul></li>');

    return nestedDirTreeView;

}

function getWC() {
    $.ajax({
        type: "GET",
        url: GET_WC,
        timeout: 2000,
        success: function (r) {
            $("#working_changes").empty();

            r.forEach(function (changes) {
                $("#working_changes").append('<p style="text-decoration: underline">' + changes.key + '</p>');
                changes.value.forEach(function (change) {
                    $("#working_changes").append('<li style="width:100%;"><a style="width:100%; font-size: 15px">' + change + '</a></li>');
                })

            });
        }
    });
}

function setPr() {
    $.ajax({
        url: GET_BRANCHES,
        timeout: 2000,
        success: function (r) {
            $('#pr-src-branch-select').empty();
            r.forEach(function (branch) {
                var optionBranch = new Option(branch.replace(" (HEAD)", ""), branch);
                $('#pr-src-branch-select').append(optionBranch);

            })
        }
    });
    $.ajax({
        url: GET_REMOTE_BRANCHES,
        timeout: 2000,
        success: function (r) {
            $('#pr-trg-branch-select').empty();
            r.forEach(function (branch) {
                var optionBranch = new Option(branch, branch);
                $('#pr-trg-branch-select').append(optionBranch);
            })
        }
    });
}

function pr() {
    $.jAlert({

        'title': 'Submit Pull Request',

        'content': '    <div>Src Branch:\n' +
            '        <select id="pr-src-branch-select" style="padding-right: 10px"></select>\n' +
            '    </div>' +
            '    <div>Target Remote Branch:\n' +
            '        <select id="pr-trg-branch-select" style="padding-right: 10px"></select>\n' +
            '    </div>' +
            '<textarea id="pr_textbox" placeholder="Please Enter Your Pull Request Massage"></textarea>'
        ,
        'theme': 'green',
        'backgroundColor': 'white',
        'showAnimation': 'fadeInUp',
        'hideAnimation': 'fadeOutDown',
        'btns': [
            {
                'text': 'Submit', 'theme': 'green', 'onClick': function () {
                    createPr()
                }
            },
            {'text': 'Cancel', 'theme': 'red', 'closeOnClick': true}
        ]
    });
    setPr();
}

function createPr() {
    var src_branch = $("#pr-src-branch-select option:selected").text();
    var trg_branch = $("#pr-trg-branch-select option:selected").text();
    var pr_msg = $('textarea#pr_textbox').val();
    $.ajax({
        url: CREATE_PR,
        data: {src_branch: src_branch, trg_branch: trg_branch, pr_msg: pr_msg},
        timeout: 2000,
        success: function (r) {
            $.jAlert({
                'title': 'Pull Request',
                'content': 'Pull Request Submitted successfully',
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown',

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
                saveBlob(blob.key, $('#save_file_box').val());
            },
            'onDeny': function () {
                $.jAlert({
                    'type': 'confirm',
                    'confirmQuestion': 'Do You Want To Delete This File ?',
                    'theme': 'red',
                    'onConfirm': function () {
                        theme = "red";
                        deleteBlob(blob.key);
                    },
                    'onDeny': function () {
                        theme = "green";
                        $.jAlert({
                            'title': 'Action Denied',
                            'content': 'File is not removed! Close the dialog by clicking the OK button',
                            'theme': theme,
                            'backgroundColor': 'white',
                            'showAnimation': 'fadeInUp',
                            'hideAnimation': 'fadeOutDown',
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
            $.jAlert({
                'title': 'File(s) Removed',
                'content': 'The files are removed!',
                'theme': theme,
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
        },
        complete: function () {
            getWC();
            initWorkingDirectory();
        }
    });
}

function pull() {
    $.ajax({
        method: 'POST',
        url: PULL,
        timeout: 2000,
        success: function (r) {
            $.jAlert({
                'title': 'Pull:',
                'content': 'Pull was successful',
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
        },
        error: function (e) {
            $.jAlert({
                'title': 'Pull:',
                'content': "Pull was unsuccessful \n\r"
                    + JSON.stringify(e),
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
        }
    });
}

function push() {
    $.ajax({
        method: 'POST',
        url: PUSH,
        timeout: 2000,
        success: function (r) {
            $.jAlert({
                'title': 'Push:',
                'content': 'Push was successful',
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
        },
        error: function (e) {
            $.jAlert({
                'title': 'Push:',
                'content': "Push was unsuccessful\n\r"
                    + JSON.stringify(e),
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
        }
    });
}

function saveBlob(blobPath, content) {
    $.ajax({
        url: SAVE_BLOB,
        data: {blobPath: blobPath, blobContent: content},
        timeout: 2000,
        success: function (r) {
            $.jAlert({
                'title': 'Save Changes!',
                'content': 'File Has Been Saved !',
                'theme': theme,
                'closeOnClick': true,
                'backgroundColor': 'black',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
        },
        complete: function () {
            getWC();
            initWorkingDirectory();
        }
    });
}

function createNewFile() {
    $.jAlert({

        'title': 'New File',

        'content': '    <div>Relative File Path:\n\r' +
            '<textarea id="file_msg_textbox" rows="1" style="width: 100%; margin-top: 0; margin-bottom: 0;" placeholder="Relative File Path..."></textarea>' +
            '    </div>'
        ,
        'theme': 'green',
        'backgroundColor': 'white',
        'showAnimation': 'fadeInUp',
        'hideAnimation': 'fadeOutDown',
        'btns': [
            {
                'text': 'Create', 'theme': 'green', 'onClick': function () {
                    createFile()
                }
            },
            {'text': 'Cancel', 'theme': 'red', 'closeOnClick': true}
        ]
    });
}

function createFile(){
    var filePath = $('textarea#file_msg_textbox').val();
    if (!filePath) {
        return;
    }

    $.ajax({
        url: CREATE_NEW_FILE,
        data: {filePath: filePath},
        timeout: 2000,
        complete: function () {
            getWC();
            initWorkingDirectory();
        }
    });
}

function showCommitModal() {

    $.ajax({
        type: "GET",
        url: GET_WC,
        timeout: 2000,
        success: function (r) {
            $("#commit_working_changes").empty();

            r.forEach(function (changes) {
                $("#commit_working_changes").append('<p style="text-decoration: underline">' + changes.key + '</p>');
                changes.value.forEach(function (change) {
                    $("#commit_working_changes").append('<li style="width:100%;"><a style="width:100%; font-size: 15px">' + change + '</a></li>');
                })

            });
        }
    });

    $('#commitModal').modal('show');
}

function hideCommitModal() {
    $('#commitModal').modal('hide');
}

function commit(commitMsg) {

    if (!commitMsg || 0 === commitMsg.length) {
        $.jAlert({
            'title': "Commit Failure",
            'content': "You Must Enter a Commit Message !",
            'theme': "red",
            'closeOnClick': true,
            'backgroundColor': 'white',
            'showAnimation': 'fadeInUp',
            'hideAnimation': 'fadeOutDown',
            'btns': [
                {'text': 'OK', 'theme': "white"}
            ]
        });
        return;
    }

    $.ajax({
        url: COMMIT,
        data: {commitMsg: commitMsg},
        timeout: 2000,
        success: function (r) {
            if (r === "No Changes Detected !") {
                $.jAlert({
                    'title': "Commit Failure",
                    'content': "No Changes Detected !",
                    'theme': "red",
                    'closeOnClick': true,
                    'backgroundColor': 'white',
                    'showAnimation': 'fadeInUp',
                    'hideAnimation': 'fadeOutDown'
                });
            } else {
                $.jAlert({
                    'title': "Commit Success",
                    'content': "Commit Was Successfully Done",
                    'theme': "blue",
                    'closeOnClick': true,
                    'backgroundColor': 'white',
                    'showAnimation': 'fadeInUp',
                    'hideAnimation': 'fadeOutDown',
                    'btns': [
                        {
                            'text': 'OK', 'theme': "white", 'onClick': function () {
                                getWC();
                                initWorkingDirectory();
                                getCommitHistory("");
                            }
                        }
                    ]
                });
            }
        }
    });
    hideCommitModal()
}

function setPrTable(){
    $.ajax({
        url: GET_PR,
        timeout: 2000,
        success: function (prs) {
            repoPrs = prs;
            var table = document.getElementById("pr-table");
            for (var i = 0; i < prs.length; i++) {
                var row = table.insertRow(i + 1);
                var cell1 = row.insertCell(0);
                var cell2 = row.insertCell(1);
                var cell3 = row.insertCell(2);
                var cell4 = row.insertCell(3);
                var cell5 = row.insertCell(4);
                var cell6 = row.insertCell(5);
                cell6.id="cell-id";
                cell1.innerHTML = prs[i].askUser;
                cell2.innerHTML = prs[i].targetBranch;
                cell3.innerHTML = prs[i].sourceBranch;
                cell4.innerHTML = prs[i].prDate;
                cell5.innerHTML = prs[i].prStatus;
                if(prs[i].prStatus.toString()!=="Open"){
                    cell6.innerHTML = '<button id="'+ prs[i].id +'" class="button" onClick="showPrChanges(this)">?</button>'
                }
                else{
                    cell6.innerHTML = '<button id="'+ prs[i].id +'" class="button" onClick="showPrChanges(this)">?</button>'+
                    '<button id="'+ prs[i].id +'" class="button" onClick="cancelPr(this)">X</button>'+
                    '<button id="'+ prs[i].id +'" class="button" onClick="approvePr(this)">V</button>'
                }
            }
        }
    });
}

function cancelPr(button){
    $.ajax({
        url: CANCEL_PR,
        data: {id:button.id},
        timeout: 2000,
        success: function () {
            $.jAlert({
                'title': "Pull Request",
                'content': "Pull Request Was Canceled",
                'closeOnClick': true,
                'backgroundColor': 'white',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
            rebuildTable()
        }
    });
}

function approvePr(button){
    $.ajax({
        url: APPROVE_PR,
        data: {id:button.id},
        timeout: 2000,
        success: function () {
            $.jAlert({
                'title': "Pull Request",
                'content': "Pull Request Was Approved",
                'closeOnClick': true,
                'backgroundColor': 'white',
                'showAnimation': 'fadeInUp',
                'hideAnimation': 'fadeOutDown'
            });
            rebuildTable()
        },
        complete: function () {
            getWC();
            initWorkingDirectory();
            getCommitHistory("");
        }
    });
}

function rebuildTable(){
    $("#pr-table").find("tr:gt(0)").remove();
    setPrTable()
}

function getPrById(id){
    var currentPr;
    repoPrs.forEach(function(pr){
        if(pr.id.toString() ===id){
            currentPr = pr;
        }
    });
    return currentPr
}

function showPrChanges(id){
    var pr = getPrById(id.id);
    $.jAlert({
        'title': pr.prMsg,
        'content': '<textarea readonly style="width: 100%;height:200px">'+pr.changes+'</textarea>',
        'size': 'full',
        'theme': "green",
        'closeOnClick': true,
        'backgroundColor': 'white',
        'showAnimation': 'fadeInUp',
        'hideAnimation': 'fadeOutDown'
    });
}

function disableButtons(){
    $.ajax({
        url: GET_REPO_INFO,
        timeout: 2000,
        success: function (r) {
            if(!r.isRemote){
                $(".remote").attr('disabled', true);
                $(".remote").removeClass('hover');
            }

        }
    });
}

$(function () { // onload...init page
    getBranches();
    getRepoInfo();
    getCommitHistory("");
    initWorkingDirectory();
    getWC();
    setPrTable();
    disableButtons();
    getUserMsg();
//     // When the user clicks on <span> (x), close the modal
//     $("#commit_modal_span").onclick = function () {
//         var modal = document.getElementById("commitModal");
//         modal.style.display = "none";
//     };
//
// // When the user clicks anywhere outside of the modal, close it
//     window.onclick = function (event) {
//         var modal = document.getElementById("commitModal");
//         if (event.target == modal) {
//             modal.style.display = "none";
//         }
//     }
});
