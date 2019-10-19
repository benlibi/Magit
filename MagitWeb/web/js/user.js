var USER_LIST_URL = buildUrlWithContextPath("getUsers");
var USER_REPO_LIST_URL = buildUrlWithContextPath("getRepos");
var UPLOAD_XML_URL = buildUrlWithContextPath("upload");
var GET_SESSION_USER_NAME = buildUrlWithContextPath("getSessionUserName");
var DO_FORK = buildUrlWithContextPath("fork");

function addUsers(usersDictionery){
    usersDictionery.forEach(function(user){
        $('.accordion').append(
        '<div class="card">' +
            '<div class="card-header" id='+ user +'>' +
            '<h2 class="mb-0">' +
            '<button class="btn btn-link" type="button" data-toggle="collapse" data-target="#collapse'+ user +'" aria-expanded="false" aria-controls="collapse'+ user +'">' +
            user +
        '</button>' +
        '</h2>' +
        '</div>' +

        '<div id="collapse'+ user +'" class="collapse" aria-labelledby="heading'+ user +'" data-parent="#accordionExample">' +
            '<div class="card-body">' +
             '<table id='+ user +'-table>' +
            '<thead>' +
            '<tr>' +
            '<th>repository</th>' +
            '<th>active branch</th>' +
        '<th>Number Of Branches</th>' +
        '<th>Last Commit</th>' +
        '<th>Last Commit Message</th>' +
        '<th></th>' +
        '</tr>' +
        '</thead>' +
        '</table>' +
        '</div>' +
        '</div>' +
        '</div>'
        )
        addUserRepos(user+"-table",user,false)
    })
}

function insertRows(table_elementId, repoRaws,isCurrent,user){
    var table = document.getElementById(table_elementId);
    for(var i=0;i< repoRaws.length;i++){
        var row = table.insertRow(i+1);
        var cell1 = row.insertCell(0);
        var cell2 = row.insertCell(1);
        var cell3 = row.insertCell(2);
        var cell4 = row.insertCell(3);
        var cell5 = row.insertCell(4);
        if(isCurrent){
            cell1.innerHTML = ' <form  id="uploadForm" action="myRepo" method="GET">' +
                '<input type="hidden" name="repoName" value="' + repoRaws[i].name+ '" />' +
                '<input class="link_button" type="Submit" value="'+ repoRaws[i].name +'">' +
                '</form>';
        }else {
            cell1.innerHTML = repoRaws[i].name;
        }
        cell2.innerHTML = repoRaws[i].headBranchName;
        cell3.innerHTML = repoRaws[i].numberOfBranches;
        cell4.innerHTML = repoRaws[i].lastCommitDate;
        cell5.innerHTML = repoRaws[i].lastCommitMessage;
        if(!isCurrent){
            var cell6 = row.insertCell(5);
            cell6.innerHTML = ' <form  id="submit-fork" ACTION="" METHOD="GET">' +
                '<input type="hidden" name="repoName" value="' + repoRaws[i].name+ '" />' +
                '<input type="hidden" name="userName" value="' + user+ '" />' +
                '<input id="submit-fork-button" type="button" value="Fork" onClick="fork(this.form)">' +
                '</form>';
        }
    }
}
function fork(e){
    $.ajax({
        method:'POST',
        data: {repoName:e.repoName.value,
            userName:e.userName.value},
        dataType: 'json',
        url: DO_FORK,
        timeout: 2000,
        success: function(r) {
            location.reload(true);
        }
    });
}

function addUserRepos(userTable,user,isCurrent){ // getRepos
    $.ajax({
        data: "user=" + user,
        dataType: 'json',
        url: USER_REPO_LIST_URL,
        timeout: 2000,
        success: function(r) {
            insertRows(userTable,r,isCurrent,user)
        }
    });
}

function setHeader(){
    $.ajax({
        url: GET_SESSION_USER_NAME,
        timeout: 2000,
        success: function (r) {
            document.getElementById("logout").value = r + " Logout";
        }
    });
}

$(function() { // onload...do   getUsers
    //add a function to the submit event
        $.ajax({
            data: $(this).serialize(),
            url: USER_LIST_URL,
            timeout: 2000,
            success: function(r) {
                addUsers(r)
            }
        });
});


$(function() { // onload...do
    addUserRepos("userTable","currentUser",true);
    setHeader()
});

$(function() { // onload...do
    $("#uploadForm").submit(function() {

        var file1 = this[0].files[0];

        var formData = new FormData();
        formData.append("fake-key-1", file1);

        $.ajax({//upload
            method:'POST',
            data: formData,
            url: UPLOAD_XML_URL,
            processData: false, // Don't process the files
            contentType: false, // Set content type to false as jQuery will tell the server its a query string request
            timeout: 4000,
            error: function(e) {
                console.error("Failed to submit");
                $("#result").text("Failed to get result from server " + e);
            },
            success: function(r) {
                $("#result").text(r);
                location.reload(true);
                // removeTableRows();
                // addUserRepos();
            }
        });
        // return value of the submit operation
        // by default - we'll always return false so it doesn't redirect the user.
        return false;
    });

});