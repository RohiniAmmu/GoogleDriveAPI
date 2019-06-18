<head>
<style type="text/css">
</style>
</head>
<form id="upload-form" action="/drive/upload" method="post" enctype="multipart/form-data">
    <input type="file" id="file" name="file1" />
    <span id="upload-error" class="error">${uploadError}</span>
    <input type="submit" id="upload-button" value="upload" />
</form>
<form id="get_folder" action="/drive/get/folder" method="get">
    <input type="text" id="folderName" name="folderName" />
    <input type="submit" id="getFolder" value="GET FOLDER DETAILS" />
</form>