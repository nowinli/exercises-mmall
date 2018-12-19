package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {
	private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

	public String upload(MultipartFile file, String path) {
		String fileName = file.getOriginalFilename();
		//获取扩展名,不需要.所以加一去掉
		String fileExtenssionName = fileName.substring(fileName.lastIndexOf(".")+1);
		//为了防止重复文件名覆盖
		String uploadFileName = UUID.randomUUID().toString()+"."+fileExtenssionName;
		logger.info("开始上传文件，上传文件名:{},上传的路径{},新文件名{}", fileName, path, uploadFileName);
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			//赋予权限可写
			fileDir.setWritable(true);
			fileDir.mkdirs();
		}
		File targetFile = new File(path, uploadFileName);
		try {
			file.transferTo(targetFile);
			//文件已经上传成功
			FTPUtil.uploadFile(Lists.newArrayList(targetFile));
			//上传到了FTP服务器
			targetFile.delete();
			//删除文件
		} catch (IOException e) {
			logger.info("上传文件异常", e);
			return null;
		}
		return targetFile.getName();
	}
}
