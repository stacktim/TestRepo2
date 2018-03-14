package jav.jmx.ServerMonitor;


public interface JavServerMonitorMBean {

    //-----------
    // operations
    //-----------
 
    //-----------
    // attributes
    //-----------
 
    // a read-only attribute called Name of type String
    public String[] dumpThreads();
    public String[] dumpTopCntThreads(int cnt);
    
    
    public String dumpClassLoadingMXBean();
    public String dumpCompilationMXBean();
    public String dumpMemoryManagerMXBeans();
    public String dumpMemoryMXBean();
    public String dumpMemoryPoolMXBeans();
    public String dumpGarbageCollectorMXBeans();
    public String dumpOperatingSystemMXBean();
    public String dumpRuntimeMXBean();
    public String dumpThreadStackTrace(long id);
    public String dumpCapturedThreads();
    
 
}
