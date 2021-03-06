Codeview = (function(){

        /* private values */
        var prj;
        var language;

        /* public methods */
        return { 
            setTitle : function( title ) {
                document.getElementById("filename").innerHTML = title;
            },

            addAction : function() {
                labels = document.getElementsByTagName("code"); 
                var varRegex = /[-_\w\d]+/ig;
                var line = "";
                var numOfMatch;
                for( var i = 0; i < labels.length; i++ ){
                    line = labels[i].innerHTML.split("&nbsp;").join(" ");
                    line = line.split("&gt;").join("<");
                    line = line.split("&lt;").join(">");
                    line = line.match(/[-_\"\d\w]+|[^-_\"\d\w]*/ig);
                    numOfMatch=0;
                    for ( var j in line ) { 
                        if ( line[j].match( /[-_\"\d\w]+/i ) ){
                            numOfMatch++;
                            line[j] = '<code class="' + labels[i].className + '" onclick="Codeview.clickHook(this)">' + line[j] + '</code>';
                        }
                    }
                    replaced = line.join('');
                    labels[i].innerHTML = replaced;
                    i += numOfMatch;
                }
            },

            postloadHook : function() {
                SyntaxHighlighter.highlight();
                Codeview.addAction();
                // load linnum
                Codeview.setTitle( window.Cindle.getFilename() );
            },

            preloadHook : function() {
                // load file
                filestr = window.Cindle.loadfile();
                $("pre").text( filestr );
                // setting syntax highlighter extension
                var brush = "plain";
                var brushDic = {"cpp":"cpp", "c":"cpp", "hpp":"cpp", "h":"cpp"
                                    ,"java":"java"
                                    ,"sh":"bash", "mk":"bash"
                                    ,"css":"css", "html":"xml", "xml":"xml"
                                    ,"php":"php", "pl":"perl"
                                    ,"py":"py", "js":"js"
                                   }; 
                var filename = window.Cindle.getFilename();
                fileExt = filename.split(".")[1];
                window.Cindle.log( "filename : " + filename + ", extension : " + fileExt );
                if( fileExt != null )
                    brush = brushDic[ fileExt ];
                else
                    window.Cindle.log( "file extension not exist" );
                if( brush == null )
                    brush = "plain"
                window.Cindle.log( "brush : " + brush );
                $("pre").addClass( "brush:" + brush );
            },

            clickHook : function( node ) {
                // console.log( node.innerHTML );
                window.Cindle.clickhook( node.innerHTML );
            },
            
        };
    })();
