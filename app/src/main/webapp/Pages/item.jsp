<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Item: ${item}</title>
    <script src="../Scripts/Libraries/jquery/jquery-1.10.2.min.js" type="text/javascript"></script>
    <script src="../Scripts/Utilities/util_xdm.js" type="text/javascript"></script>
    <script src="../Scripts/client.js" type="text/javascript"></script>
    <script type="text/javascript">
        function loadItem(){
            IRiS.setFrame(frames[0]);
            // set the vendor guid.
            //Note: in the OSS IRiS case we do not care for this.
            var vendorId = '2B3C34BF-064C-462A-93EA-41E9E3EB8333';
            var token = '${token}';
            IRiS.loadToken(vendorId, token);
        };
        function irisSetup() {
            window.Util.XDM.addListener('IRiS:ready', loadItem);
        };
    </script>
    <style>
        body {
            margin: 0;
        }
        iframe {
            display: block;
            border: none;
            height: 100vh;
            width: 100vw;
        }
    </style>
</head>
<body>
<iframe id="irisWindow" src="${pageContext.request.contextPath}/" onload="irisSetup()"></iframe>
</body>
</html>
