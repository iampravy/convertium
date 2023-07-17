
$(document).ready(function(){
    $("#articleForm").on("submit", function(event){
        event.preventDefault();
        var formValues= $(this).serialize();
        var url = $(".form-url").data('url');
        $.post(url, formValues, function(data){
            // Display the returned data in browser
            $(".result").html(data);
        });
    });
});