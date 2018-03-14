<%@ page language="java" contentType="text/html; charset=ISO-8859-1"  pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.lang.management.GarbageCollectorMXBean,java.lang.management.ManagementFactory,java.lang.management.MemoryPoolMXBean,java.lang.management.MemoryUsage,java.lang.management.ThreadInfo, java.lang.management.ThreadMXBean" %>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,java.util.Arrays,java.util.List,java.util.HashMap" %>

<%
        String pids = request.getParameter("id");
        long pidsList[];
    if( pids!=null && pids.length()>0){
            String[] pidList = pids.split(",");
                pidsList= new long[pidList.length];

                for(int i=0;i<pidList.length;i++){
                        pidsList[i] = Long.parseLong(pidList[i]);
                }
    } else {
        pidsList= new long[0];
    }
%>

<%!
        public  String getMemoryJson(){
                StringBuffer sb= new StringBuffer("[");
                List<MemoryPoolMXBean> mpbBeans = ManagementFactory.getMemoryPoolMXBeans();
                for (MemoryPoolMXBean memoryPoolMXBean : mpbBeans) {
                        MemoryUsage m = memoryPoolMXBean.getUsage();
                        sb.append("{name: '" +  memoryPoolMXBean.getName() + "', usage:{");
                        sb.append("init:" + m.getInit() + ", used:" + m.getUsed() + ", commited:" + m.getCommitted() + ", max:" + m.getMax());
                        sb.append("}},");
                }
                sb.append("]");
                return (sb.toString());
        }

                public   String getGCToJson() {
                        StringBuffer sb = new StringBuffer();
                        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
                        for (GarbageCollectorMXBean gcBean : gcBeans) {
                                sb.append("{name: '" + gcBean.getName() + "'"
                                + ", count:" + gcBean.getCollectionCount()
                                + ", time:" + gcBean.getCollectionTime()
                                + "}," );
                        }
                        return "[" + sb.toString() + "]";
                }
                public   String getThreadIdMapping() {
                        StringBuffer sb = new StringBuffer();
                        ThreadMXBean tmBean = ManagementFactory.getThreadMXBean();
                        long[] tids = tmBean.getAllThreadIds();
                        ThreadInfo[] tifs = tmBean.getThreadInfo(tids, Integer.MAX_VALUE);
                        HashMap<String, ThreadInfo> hm = new HashMap<String, ThreadInfo>();
                        for (int i = 0; i < tifs.length; i++) {
                                ThreadInfo threadInfo = tifs[i];
                                long tid = threadInfo.getThreadId();
				//if ( threadInfo.getThreadName().startsWith("WebC") ){
                                	hm.put(threadInfo.getThreadName(), threadInfo);
                                //}
                        }
                        
                        
                        String kNames[] = new String[hm.size()];
                        hm.keySet().toArray(kNames);
                        Arrays.sort(kNames);
                        for (int i = 0; i < kNames.length; i++) {
                            	ThreadInfo tif = hm.get(kNames[i]);
                            	
                                sb.append("{id:'" + tif.getThreadId() +  "',  name:'" +  kNames[i] + "', state:'" +
                                		tif.getThreadState() + "'},");
                        }
                        return "[" + sb.toString() + "]";
                }
                public   String getThreadToJson(long []tids) {
                        StringBuffer sb = new StringBuffer();
                        ThreadMXBean tmBean = ManagementFactory.getThreadMXBean();
                        //long[] tids = tmBean.getAllThreadIds();
                        ThreadInfo[] tifs = tmBean.getThreadInfo(tids, Integer.MAX_VALUE);
                        for (int i = 0; i < tifs.length; i++) {
                                ThreadInfo threadInfo = tifs[i];
                                long tid = threadInfo.getThreadId();
                                sb.append("{"
                                + "id:" + tid + ", name:'" + threadInfo.getThreadName() + "', state:'" + threadInfo.getThreadState()
                                + "', cpuTime:" + tmBean.getThreadCpuTime(tid)
                                + ", userTime:" + tmBean.getThreadUserTime(tid)
                                + "},");
                        }
                        return "[" + sb.toString() + "]";
                }
%>
<html>
<head>

<style>
TD.RUNNABLE {
    background-color: green;
}
TD.BLOCKED {
    background-color: red;
}
</style>


<script>

var jsMemOb    = <%=  getMemoryJson()  %>;
var jsGCOb     = <%=  getGCToJson()  %>;
var jsThrdIdOb = <%=  getThreadIdMapping()  %>;
var jsThrdOb   = <%=  getThreadToJson(pidsList) %>;
var list       = {};
var pids = "<%= pids %>";
function go(id){
        var group = document.getElementsByName('cbList1');
        var sb="?id=";
        var firstSet=false;
        for (var i=0; i<group.length; i++) {
                if ( group[i].checked ) {
                        if (firstSet==false){
                                firstSet=true;
                                sb += group[i].id;
                        }else{
                                sb += "," + group[i].id;
                        }
                }
        }
        location.href = location.href + sb;
}

var hmn = ["b", "Kb", "Mb", "Gb"];
function convertToHuman( bytes){
        var i=0;
        while (bytes>1024){
                i++;
                bytes=bytes/1024;
        }


        return bytes.toFixed(2) + " "+ hmn[i];

}


</script>
<title>JMX Memory</title>
</head>

<body>


<TABLE>
        <TR>
        <TH>Name
        <TH>count
        <TH>Time

<script>
{
        var cnt = jsGCOb.length;
        for( var i=0;i<cnt; i++){
                document.write( "<TR>" );
                document.write( "<TH>" + jsGCOb[i].name );
                document.write( "<TD>" + jsGCOb[i].count );
                document.write( "<TD>" + jsGCOb[i].time );
        }
}

</script>
</TABLE>

<br/>
<br/>

<TABLE>
        <TR>
        <TH>Name
        <TH>Init
        <TH>Used
        <TH>Commited
        <TH>Max
        <TH>%Commited
        <TH>%Max



<script>
{
        var cnt = jsMemOb.length;
        for( var i=0;i<cnt; i++){
                document.write( "<TR align='center'>" );
                document.write( "<TH>" + jsMemOb[i].name );
                document.write( "<TD>" + convertToHuman(jsMemOb[i].usage.init) );
                document.write( "<TD>" + convertToHuman(jsMemOb[i].usage.used) );
                document.write( "<TD>" + convertToHuman(jsMemOb[i].usage.commited) );
                document.write( "<TD>" + convertToHuman(jsMemOb[i].usage.max) );
                document.write( "<TD>" + (jsMemOb[i].usage.used/jsMemOb[i].usage.commited*100).toFixed(2) );
                if ( jsMemOb[i].usage.max>0 ){
                        document.write( "<TD>" + ((jsMemOb[i].usage.used/jsMemOb[i].usage.max)*100).toFixed(2) );
                } else {
                        document.write("<TD>-");
                }
        }
}

</script>
</TABLE>



<br/><br/><br/>

<TABLE>
        <TR>
        <TH>Name
        <TH>State
        <TH>Name
        <TH>State
        <TH>Name
        <TH>State

<script>
{
	var cnt = jsThrdIdOb.length;
	for( var i=0;i<cnt; i++){
    	if (i%3 == 0 ){
        	document.write( "<TR>" );
        }
        document.write( "<TD>" +  jsThrdIdOb[i].name  + "<TD class='" + jsThrdIdOb[i].state + "'>" +  jsThrdIdOb[i].state);
	}
}

</script>

</TABLE>



</body>
</HTML>

