var xhr = null;
function clear_all(){
    //clear result text
    $("#resultText").text("");
    //hide result section
    $(".result").addClass("hidden");
    //clear input file
    $("#form")[0].uploadFile.value  = "";
    //disable request
    //kill the request
    xhr.abort();
}

function upload(file){
    var file = uploadFile.files[0];
    if(file == undefined){
        $("#resultText")
            .text("Please upload a file")
            .addClass("error");
        $(".result").removeClass("hidden");
    }else{
        //clear result text
        $("#resultText").text("");
        //clear the previous error result
        $("#resultText").removeClass("error");


        var selectedLanguage = $("#lang").text();
        var timeout = 0;//default no limitation
        if(selectedLanguage== "Chinese") timeout = 120000; // 2 minutes

        var form = $("#form")[0];
        var data = new FormData(form);
        xhr = $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: "/upload?language=" + $("#lang").text(),
            data: data,
            processData: false, //prevent jQuery from automatically transforming the data into a query string
            contentType: false,
            cache: false,
            timeout: timeout,
            beforeSend: function(){
                $(".result").removeClass("hidden");
                $("#resultText").text("Recognizing...");
                $("#analyseBtn").addClass("disableMouseClick");//to prevent click to analayse button
            },
            success: function (data) {
                $(".result").removeClass("hidden")
                $("#resultText").text(data);
            },
            error: function (e) {
                //proxy upload error
                if(e.readyState == 4 && e.status == 403){
                    e.responseText = e.statusText;
                }

                //badgateway & timeout
                if( (e.readyState == 4 && e.status == 502) || (e.readyState == 0 && e.status == 0) ){
                    if(e.statusText !="abort"){
                        e.responseText = "No Response From Server. Please check your connection or the server is still rendering your audio file.";
                    }
                }

                if(e.statusText !="abort"){
                    $("#resultText")
                        .text(e.responseText)
                        .addClass("error");
                    $(".result").removeClass("hidden");
                }

            },
            complete: function(jqXHR,textStatus){
                 var pageHeight = $(document).height();
                 window.scrollBy(0,pageHeight);

                 $("#analyseBtn").removeClass("disableMouseClick");
            }
        });
    }
}

function setLanguage(language){
    var lang = $(language).text().trim();
    if(lang != null){
        if(lang === "Chinese"){
            $("#en").addClass("hidden");
            $("#zh").removeClass("hidden");
        }else if(lang === "English"){
            $("#zh").addClass("hidden");
            $("#en").removeClass("hidden");
        }else{
            alert("Please select a language");
        }
    }
    //set text to language field
    $("#lang").text(lang);

}