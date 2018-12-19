<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<body>
<h2>Hello World!</h2>

上传文件
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="上传">
</form>

上传文件2
<form name="form1" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file">
    <input type="submit" value="上传2">
</form>
</body>
</html>
