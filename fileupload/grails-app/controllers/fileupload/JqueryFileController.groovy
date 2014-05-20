package fileupload

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import grails.converters.JSON
import groovy.util.ConfigObject;

import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartFile
import org.imgscalr.Scalr
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.CropImageFilter
import java.awt.image.FilteredImageSource
import java.awt.image.ImageFilter
import javax.imageio.ImageIO
import java.awt.Toolkit;

class JqueryFileController{
	def index ={
		
	}
	def doPost = {
	def results = [files:[]]
                if (request instanceof MultipartHttpServletRequest){
                    for(filename in request.getFileNames()){
                        MultipartFile file = request.getFile(filename)

                        String newFilenameBase = UUID.randomUUID().toString()
                        String originalFileExtension = file.originalFilename.substring(file.originalFilename.lastIndexOf("."))
                        String newFilename = newFilenameBase + originalFileExtension
                        String storageDirectory = "C:/img"
						//String storageDirectory = grailsApplication.config.file.upload.directory?:'/tmp'

                        File newFile = new File("C:/img/$newFilename")
						//File newFile = new File("$storageDirectory/$newFilename")
                        file.transferTo(newFile)
						
						//BufferedImage thumbnail = Scalr.resize(ImageIO.read(newFile), 200);
						String thumbnailFilename = newFilenameBase + '-thumbnail.png'
						//File thumbnailFile = new File("$storageDirectory/$thumbnailFilename")
						//ImageIO.write(thumbnail, 'png', thumbnailFile)
					 cut(ImageIO.read(newFile), 200, 200, "$storageDirectory/$thumbnailFilename");
					 def deleteid = [pic:newFilename, thumbnail:thumbnailFilename]
                       results.files << [
                                name: newFilename,
								size:file.size,
								url: createLink(controller:'jqueryFile', action:'picture', id: newFilename),
								thumbnailUrl: createLink(controller:'jqueryFile', action:'thumbnail', id: thumbnailFilename),
								deleteUrl: createLink(controller:'jqueryFile', action:'delete',params:deleteid),
                                deleteType: "DELETE"
                        ]
                    }
                }
                render results as JSON
	}
	 def picture = {
        def pic = params.id
        File picFile = new File("${grailsApplication.config.file.upload.directory?:'C:/img'}/${pic}")
        response.contentType = 'image/jpeg'
        response.outputStream << new FileInputStream(picFile)
        response.outputStream.flush()
    }

    def thumbnail = {
        def pic = params.id
        File picFile = new File("${grailsApplication.config.file.upload.directory?:'C:/img'}/${pic}")
        response.contentType = 'image/png'
        response.outputStream << new FileInputStream(picFile)
        response.outputStream.flush()
    }

    def delete={
        def pic = params.pic
		def thumbnail = params.thumbnail
        File picFile = new File("${grailsApplication.config.file.upload.directory?:'C:/img'}/${pic}")
        picFile.delete()
        File thumbnailFile = new File("${grailsApplication.config.file.upload.directory?:'C:/img'}/${thumbnail}")
        thumbnailFile.delete()
       // pic.delete()

        def result = [success: true]
        render result as JSON
    } 
	
	def getMimeType(File file) {
        String mimetype = "";
        if (file.exists()) {
            if (getSuffix(file.getName()).equalsIgnoreCase("png")) {
                mimetype = "image/png";
            }else if(getSuffix(file.getName()).equalsIgnoreCase("jpg")){
                mimetype = "image/jpg";
            }else if(getSuffix(file.getName()).equalsIgnoreCase("jpeg")){
                mimetype = "image/jpeg";
            }else if(getSuffix(file.getName()).equalsIgnoreCase("gif")){
                mimetype = "image/gif";
            }else {
                javax.activation.MimetypesFileTypeMap mtMap = new javax.activation.MimetypesFileTypeMap();
                mimetype  = mtMap.getContentType(file);
            }
        }
        return mimetype;
    }
	
	def getSuffix(String filename) {
        String suffix = "";
        int pos = filename.lastIndexOf('.');
        if (pos > 0 && pos < filename.length() - 1) {
            suffix = filename.substring(pos + 1);
        }
        return suffix;
    }
	/*
	* @param bi
	*            BufferedImage 图片对象
	* @param w
	*            切割宽度
	* @param h
	*            切割高度
	* @param destPath
	*/
	def cut(BufferedImage bi, int w, int h,  String destPath) {
	   try {
		Image img;
		ImageFilter cropFilter;
	
		//获得缩放的比例
		double ratio = 0.0;
		if(bi.getHeight()>200 || bi.getWidth()>200)
		{
		 if(bi.getHeight() > bi.getWidth())
		 {
		  ratio = 200.0 / bi.getHeight();
		 }
		 else
		 {
		  ratio = 200.0 / bi.getWidth();
		 }
		}
		//计算新的图面宽度和高度
		int newWidth =(int)(bi.getWidth()*ratio);
		int newHeight =(int)(bi.getHeight()*ratio);
		 Image image = bi.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
		 // 四个参数分别为图像起点坐标和宽高
		 // 即: CropImageFilter(int x,int y,int width,int height)
		int xx = image.getWidth()/2-100;
		int yy = image.getHeight()/2-100;
		 cropFilter = new CropImageFilter(xx, yy, w, h);
		 img = Toolkit.getDefaultToolkit().createImage(
		   new FilteredImageSource(image.getSource(), cropFilter));
		BufferedImage tag = new BufferedImage(w, h,
		   BufferedImage.TYPE_INT_RGB);
		 Graphics g = tag.getGraphics();
		 g.drawImage(img, 0, 0, null); // 绘制缩小后的图
		 g.dispose();
		 OutputStream out = new FileOutputStream(new File(destPath));
		 // 输出为文件
		 ImageIO.write(tag, "JPEG", out);
		 out.flush();
		 out.close();
	
	   } catch (Exception e) {
		e.printStackTrace();
	   }
	  
	}
	
}
