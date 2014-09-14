Xpert = {
    clearDownloadCookie: function(){
        $.cookie('xpert.download', null, {
            path: '/', 
            expires: -1
        });
    },
    createOverlayWithDialog: function(id, message){
        //create overlay
        var dialog = '<div class="ui-widget-overlay" style="z-index: 10000" id="'+id+'_overlay"></div>'+
                     '<div class="overlay-download ui-widget" style="z-index: 10001" id="'+id+'"><div class="content"><p>'+message+'</p></div>';
        $(document.body).append(dialog);
        $("#"+id+","+"#"+id+"_overlay").css({
            'width': $(document).width() ,
            'height': $(document).height()
        });
    },
    removeOverlayWithDialog : function(id){
         $("#"+id).remove();
         $("#"+id+"_overlay").remove();
    },
    highlightCode : function(name) {
       dp.SyntaxHighlighter.HighlightAll(name);
    },
    popupTextArea : function(selector) {
        var content = $(selector).html();
        var wnd =window.open('','_blank','width=750, height=400, location=0, resizable=1, menubar=0, scrollbars=0');
        wnd.document.write('<textarea style="width:99%;height:99%">'+content+'</textarea>')
    },
    detailAuditTable : function(element) {
        $span = $(element).find(".ui-button-icon-left"); 
        if($span.hasClass("ui-icon-plus")){
            $span.removeClass("ui-icon-plus");
            $span.addClass("ui-icon-minusthick");
        }else{
            $span.removeClass("ui-icon-minusthick");
            $span.addClass("ui-icon-plus");
        } 
        $(element).closest("tr").next("tr").toggle("fast");
    },
    skinButton : function(element) {
        $(element).mouseover(function(){
            $(this).addClass('ui-state-hover');
        }).mouseout(function() {
            $(this).removeClass('ui-state-active ui-state-hover');
        }).mousedown(function() {
            $(this).addClass('ui-state-active');
        }).mouseup(function() {
            $(this).removeClass('ui-state-active');
        }).focus(function() {
            $(this).addClass('ui-state-focus');
        }).blur(function() {
            $(this).removeClass('ui-state-focus');
        }).keydown(function(e) {
            if(e.keyCode == $.ui.keyCode.SPACE || e.keyCode == $.ui.keyCode.ENTER || e.keyCode == $.ui.keyCode.NUMPAD_ENTER) {
                $(this).addClass('ui-state-active');
            }
        }).keyup(function() {
            $(this).removeClass('ui-state-active');
        });
        
        return this;
    },
    dateFilter : function(element) {
        var dateId = PrimeFaces.escapeClientId(element);
        var $column = $(dateId).closest('.ui-filter-column');
        var $inputFilter = $column.find('.ui-column-filter');
        
        var dateStart = $column.find('.calendar-filter-start input').val();
        var dateEnd = $column.find('.calendar-filter-end input').val();
        var concatDate = dateStart+" ## "+dateEnd;
        $inputFilter.val(concatDate);

    },
    clearDateFilter : function(element) {
        $(element).closest('th').find('input').val('');
    },
    refreshDateFilter : function(column, dateStart, dateEnd) {
        var $column  = $(PrimeFaces.escapeClientId(column));
        $column.find('.calendar-filter-start input').val(dateStart);
        $column.find('.calendar-filter-end input').val(dateEnd);
    },
    filterOnEnter: function(target, selector){
        
         if(target != null && target != "" && target != undefined){
             selector = PrimeFaces.escapeClientId(target);
         }
         if(selector == null || selector == "" || selector == undefined){
             selector = ".ui-datatable";
         }
         
         $("body").delegate(selector+" .ui-filter-column input", "focus", function(e){
            var events = $._data(this, "events");
            var originalEvent;
            $.each(events, function(i, event) {
                if(i == "keyup" || i == "keydown"){
                    originalEvent = event[0].handler;
                    return;
                }
            });
            $(this).unbind("keydown").unbind("keyup");
            $(this).keyup(function(keyup) {
                if (keyup.keyCode == 13) {
                    keyup.preventDefault();
                } 
            }).keydown(function(keydown) {
                if (keydown.keyCode == 13) {
                    keydown.preventDefault();
                    originalEvent(keydown);
                } 
            });
        });
    }, 
    spreadCheckBoxList: function(id, columns, highlight){
        var $table = $("input[id ^= "+(id.replace(/:/g,"\\:")+"]:first")).closest("table");
        var $td = $table.find("tr td");
        var inner = "";
        var total = 0;
        jQuery.each($td, function(i, element) {
            if(total == 0){
                inner = inner+"<tr>";
            }
            var checkbox = $(element).find("input[type=checkbox],input[type=radio]");
            if(highlight && checkbox[0].checked == true){
                inner = inner+"<td class='ui-state-highlight' style='border: 0;'>";
            }else{
                inner = inner+"<td>";   
            }
            inner = inner+element.innerHTML+"</td>";
            if(total == columns-1){
                inner = inner+"</tr>";
                total = 0;
            }else{
                total++;
            }
        });
        $table.html(inner);
        if(highlight == true){
            $table.find("input[type=checkbox],input[type=radio]").click(function() {
                var $td = $(this).closest("td");
                $td.css("border", 0);
                if($(this).attr("type") == "radio"){
                    $table.find("td").removeClass("ui-state-highlight");
                }
                if(this.checked){
                    $td.addClass("ui-state-highlight");
                }else{
                    $td.removeClass("ui-state-highlight");
                }
            });
        }
    }
};
Xpert.behavior = {
    
     verifyConfirmation: function (element, confirmLabel, cancelLabel, message, primefaces3){
        var $element = $(element); 
        var onclick = $element.attr("onclick");
        
        if(onclick != null && onclick != undefined && onclick.length > 0){
            onclick = onclick.replace(/this/g,"'"+$(element).attr("id")+"'");

            $element.removeAttr("onclick");
            $element.click(function () {
                Xpert.behavior.confirmation(confirmLabel,cancelLabel, message, onclick, primefaces3);
                return false;
            });
        }

     },
    
     confirmation: function(confirmLabel, cancelLabel, message, onclick, primefaces3) {
        //create dialog
        var id = "idWidgetConfirmationDialog";
        var widgetVar = "widgetConfirmationDialog";
        if (primefaces3 == false){
            widgetVar = "PF('"+widgetVar+"')";
        }
        var confirmClick = widgetVar+".hide();"+onclick+";return false;";;
        //create only one time
        var $createdDialog = $("#"+id);
        if($createdDialog != null && $createdDialog.length > 0){
            $createdDialog.find(".dialog-confirm-button span").html(confirmLabel);
            $createdDialog.find(".dialog-cancel-button span").html(cancelLabel);
            $createdDialog.find(".dialog-confirm-message").html(message);
            $createdDialog.find("#xpertCofirmationButton").attr("onclick", confirmClick);
        }else{
            var html =  '<div style="visibility: visible;" class="ui-confirm-dialog ui-dialog ui-widget ui-widget-content ui-corner-all ui-shadow" id="'+id+'" >'
                            +'<div class="ui-dialog-content ui-widget-content" style="height: auto;">'
                                    +'<p><span class="ui-icon ui-icon-alert ui-confirm-dialog-severity"></span><span class="dialog-confirm-message">'+message+'</span></p>'
                            +'</div>'
                            +'<div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">'
                                +'<form>'
                                    +'<button id="xpertCofirmationButton" type="submit" onclick="'+confirmClick+'" class="dialog-confirm-button ui-state-default ui-button ui-widget ui-corner-all ui-button-text-only" role="button" aria-disabled="false">'
                                        +'<span class="ui-button-text">'+confirmLabel+'</span>'
                                    +'</button>'
                                    +'<button type="button" onclick="'+widgetVar+'.hide()" class="dialog-cancel-button ui-state-default ui-button ui-widget ui-corner-all ui-button-text-only" role="button" aria-disabled="false">'
                                        +'<span class="ui-button-text">'+cancelLabel+'</span>'
                                    +'</button>'
                                    +'<script type="text/javascript">Xpert.skinButton("#'+id+' button");</script>'
                                +'</form>'
                            +'</div>'
                        +'</div>';
            html = html+'<script type="text/javascript">PrimeFaces.cw("Dialog","widgetConfirmationDialog",{id:"'+id+'",modal:true,resizable:false,width:300,visible:true});</script>';
            $(html).appendTo("body");
        }  
        if (primefaces3 == true){
            widgetConfirmationDialog.show();
        }else{
            PF('widgetConfirmationDialog').show();
        }
        $("#"+id).find(".dialog-confirm-button").focus();
     },

    download: function(object, cfg) {
        if(cfg.onstart) {
            cfg.onstart();
        }
        Xpert.clearDownloadCookie();
        var token = $($(object).closest("form")).find("input[id=javax\\.faces\\.ViewState]").val();
        if(token == null || token == ""){
            return;
        }
        if(cfg.showModal){
            Xpert.createOverlayWithDialog("dialog-download", cfg.message);
        }
        var poll = setInterval(function() {
            var cookie = $.cookie('xpert.download', {
                path: '/'
            });
            if (cookie != null && cookie.replace(/\"/g,"") == token.replace(/\"/g,"")){
                if(cfg.oncomplete) {
                    cfg.oncomplete();
                }
                Xpert.clearDownloadCookie();
                if(cfg.showModal){
                    Xpert.removeOverlayWithDialog("dialog-download");
                }
                clearInterval(poll);
            }
        }, 500);
    }

};