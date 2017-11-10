package project.gunay.demoSTT.controllers;

import project.gunay.demoSTT.stt.HciCloudAsr;
import project.gunay.demoSTT.stt.Sphinx4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Gunay Gultekin on 5/22/2017.
 */
@Controller
@RequestMapping("/")
public class HomePageController {

    @RequestMapping(method = RequestMethod.GET)
    public String home(Model model){
        return "home";
    }

    @RequestMapping(value="/upload")
    @ResponseBody
    public ResponseEntity<String> userSingleFileUpload(
            @RequestParam("uploadFile") MultipartFile file,
            @RequestParam("language") String language){
        String text;
        System.out.println("Enter userSingleFileUpload");

        System.out.println("language is set to -> " + language);

        if(file.getSize() == 0){
            text = "Can Not send an empty file!";
            System.out.println(text + " file:" + file.getOriginalFilename());
            return new ResponseEntity<>(text,HttpStatus.BAD_REQUEST);
        }

        String renderResult;
        if(language.equals("Chinese")){
            renderResult = HciCloudAsr.runTool(file);

            if(renderResult==null || renderResult.contains("Param Invalid")){
                text = "Media type is not supported! Please upload an supported audio file.";
                System.out.println(text + " file:" + file.getOriginalFilename());
                return  new ResponseEntity<>(text,HttpStatus.BAD_REQUEST);
            }

            if(renderResult.contains("error")){
                text = renderResult;
                return new ResponseEntity<>(text, HttpStatus.BAD_REQUEST);
            }else{
                text = renderResult;
                return new ResponseEntity<>(text, HttpStatus.OK);
            }
        }else if (language.equals("English")){
            try {
                renderResult = Sphinx4.runTool(file);
                if(renderResult.length() > 0){
                    return new ResponseEntity<>(renderResult,HttpStatus.OK);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return  new ResponseEntity<>("Error is occurred,Please contact system administrator",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Please select a language",HttpStatus.BAD_REQUEST);
    }
}
