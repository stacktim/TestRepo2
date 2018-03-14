package jav.jmx.ServerMonitor;


import jav.jmx.Helper.HashMapArrayList;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class JavServerMonitor implements JavServerMonitorMBean {

	protected static final SimpleDateFormat sdf_MDYHMS = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
    public String dumpClassLoadingMXBean(){
		return ReflectionClassCalling(ManagementFactory.getClassLoadingMXBean());
    }
    public String dumpCompilationMXBean(){
		return ReflectionClassCalling(ManagementFactory.getCompilationMXBean());
    }

    public String dumpMemoryManagerMXBeans(){
		return ReflectionClassCalling(ManagementFactory.getMemoryManagerMXBeans());
    }

    public String dumpMemoryMXBean(){
		return ReflectionClassCalling(ManagementFactory.getMemoryMXBean());
    }

    public String dumpMemoryPoolMXBeans(){
		return ReflectionClassCalling(ManagementFactory.getMemoryPoolMXBeans());
    }

    public String dumpGarbageCollectorMXBeans(){
		return ReflectionClassCalling(ManagementFactory.getGarbageCollectorMXBeans());
    }

    public String dumpOperatingSystemMXBean(){
		return ReflectionClassCalling(ManagementFactory.getOperatingSystemMXBean());
    }
    public String dumpRuntimeMXBean(){
		return ReflectionClassCalling(ManagementFactory.getRuntimeMXBean());
    }
	
	private String ReflectionClassCalling(Object obj ){
		StringBuffer sb = new StringBuffer();
		Class c = obj.getClass();
		sb.append(c.getCanonicalName());
		sb.append("\n");
		
		for (Method method : c.getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
				Object value;
				try {
					value = method.invoke(obj);
				} catch (Exception e) {
					value = e;
				} // try

				sb.append(method.getName() + " = " + value);
				sb.append("\n");
			} // if
		} // for
		
		return sb.toString() + "\n";
	}
	/*
	 * 
	 *  
	 * State.BLOCKED
	 *  State.NEW
	 *  State.RUNNABLE
	 *  State.TERMINATE
	 *  State.TIMED_WAITING
	 *  State.WAITING
	 */
	@Override
	public String[] dumpThreads() {
		ArrayList<String> list = new ArrayList<String>();
		ThreadMXBean tmxBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] tiArray = tmxBean.dumpAllThreads(false, false);
		for (int i = 0; i < tiArray.length; i++) {
			ThreadInfo ti = tiArray[i];
			if( ti.getThreadState() == State.BLOCKED || ti.getThreadState() == State.RUNNABLE){
				list.add(ti.getThreadName());
			}
		}
		return list.toArray(new String[0]);
	}


	@Override
	public String[] dumpTopCntThreads(int cnt) {
		ThreadMXBean tmxBean = ManagementFactory.getThreadMXBean();
		HashMapArrayList<Long, ThreadInfo> tiList = new HashMapArrayList<Long,ThreadInfo>();
		{
			ThreadInfo[] tiArray = tmxBean.dumpAllThreads(false, false);
			for (int i = 0; i < tiArray.length; i++) {
				ThreadInfo ti = tiArray[i];
				Long thrRnTime = tmxBean.getThreadCpuTime(ti.getThreadId())/1000000;
				if ( thrRnTime>5 ){
					tiList.AddToMap(thrRnTime, ti);
				}
			}
		}	
		Long sortedThreadList[] =null;
		{
			Set<Long>cList  =tiList.getKeys();
			sortedThreadList = cList.toArray(new Long[0]);
			Arrays.sort(sortedThreadList, Collections.reverseOrder());
		}
		
		String[] retList = new String[cnt];
		{
			int retStringCnt= 0;
			int retListIdx=0;
			while(retStringCnt<cnt && retListIdx<sortedThreadList.length) {
				ArrayList<ThreadInfo> cTis = tiList.getValue(sortedThreadList[retListIdx++]);
				
				for (int j = 0; j < cTis.size(); j++) {
					ThreadInfo myTid = cTis.get(j);
					if( retStringCnt<cnt  && retStringCnt<cTis.size()){
						retList[retStringCnt++] = sortedThreadList[retListIdx++] + " \t " + myTid.getThreadState() + " \t " + myTid.getThreadId() + "\t" + myTid.getThreadName();
					} else {
						return retList;
					}
				}
			}
		}
		return retList;
	}
	@Override
	public String dumpThreadStackTrace(long id) {
		System.out.println( id );
		long[] ids = new long[]{id};
		System.out.println( ids[0] );
		try{
			ThreadMXBean tmxBean = ManagementFactory.getThreadMXBean();
			ThreadInfo[] tiArray = tmxBean.getThreadInfo(ids, true, true);
			
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < tiArray.length; i++) {
				sb.append( printThreadInfo( tiArray[i]) );
			}
			return sb.toString();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private String printThreadInfo(ThreadInfo ti) {
		String INDENT = "   " ;
		StringBuffer sb = new StringBuffer();
		if ( ti == null){
			return "NO THREAD";
		}
		sb.append("\"" + ti.getThreadName() + "\"" + " Id=" + ti.getThreadId() + " in " + ti.getThreadState());
		
		if (ti.getLockName() != null) {
			sb.append(" on lock=" + ti.getLockName()+"\n");
		}
		if (ti.isSuspended()) {
			sb.append(" (suspended)"+"\n");
		}
		if (ti.isInNative()) {
			sb.append(" (running in native)"+"\n");
		}
		
		if (ti.getLockOwnerName() != null) {
			sb.append(INDENT + " owned by " + ti.getLockOwnerName() + " Id=" + ti.getLockOwnerId() + "\n");

		}
		
		// print stack trace with locks
		StackTraceElement[] stacktrace = ti.getStackTrace();
		MonitorInfo[] monitors = ti.getLockedMonitors();
		for (int i = 0; i < stacktrace.length; i++) {
			StackTraceElement ste = stacktrace[i];
			sb.append(INDENT + "at " + ste.toString() + "\n");
			for (MonitorInfo mi : monitors) {
				if (mi.getLockedStackDepth() == i) {
					sb.append(INDENT + "  - locked " + mi + "\n");
				}
			}
		}
		
		LockInfo[] syncs = ti.getLockedSynchronizers();
		sb.append(INDENT + "Locked synchronizers: count = " + syncs.length + "\n");
		for (LockInfo li : syncs) {
			sb.append(INDENT + "  - " + li + "\n");
		}
		

		sb.append(INDENT + "Locked monitors: count = " + monitors.length + "\n");
		for (MonitorInfo mi : monitors) {
			sb.append(INDENT + "  - " + mi + " locked at " + "\n");
			sb.append(INDENT + "      " + mi.getLockedStackDepth() + " " + mi.getLockedStackFrame() + "\n");
		}

		return sb.toString()+"\n";
	}
	@Override
	public String dumpCapturedThreads() {
		return createMessage();
	}
	

	private static HashMap<Long, ArrayList<ThreadInfo>>   getBlockedThreads(ThreadMXBean tmx){
		HashMap<Long, ArrayList<ThreadInfo>> retValue = new HashMap<Long, ArrayList<ThreadInfo>>();
		
		ThreadInfo tis[] = tmx.dumpAllThreads(true, true);
		for (int i = 0; i < tis.length; i++) {
		
			//System.out.println(p[i]);
			ThreadInfo ti = tis[i];
			
			if (!(  ti.getThreadName().startsWith("We") || ti.getThreadName().startsWith("Thread") ) ){
				//System.out.println(ti.getThreadId() + ":" + ti.getThreadName() + "\t" + ti.getThreadState().toString());
				continue;
			}
			
			Thread.State thrd_state = ti.getThreadState();
			if ( thrd_state == Thread.State.BLOCKED){
				long lockHold = ti.getLockOwnerId();
				if(retValue.containsKey(lockHold) ==false ){
					retValue.put(lockHold, new ArrayList<ThreadInfo>());
				}		
				retValue.get(lockHold).add(ti);
			}
		}
		return retValue;
	}
	
	

	public String createMessage(){
		StringBuffer sb = new StringBuffer();
		ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
		
		HashMap<Long, ArrayList<ThreadInfo>> l = getBlockedThreads(tmx);
		
		Set<Long> s = l.keySet();
		long []lockIdarr = new long[1];
		for (Iterator<Long> iterator = s.iterator(); iterator.hasNext();) {
			lockIdarr[0] =  iterator.next();
			ThreadInfo[] ti = tmx.getThreadInfo(lockIdarr,true, true);
			sb.append(sdf_MDYHMS.format(new Date(System.currentTimeMillis())) +"\n");
			sb.append(printThreadInfo(ti[0]) + "\n");
			
			
			
			ArrayList<ThreadInfo> tis = l.get(lockIdarr[0]);
			for (Iterator<ThreadInfo> iterator2 = tis.iterator(); iterator2.hasNext();) {
				ThreadInfo threadInfo = (ThreadInfo) iterator2.next();
				sb.append(printThreadInfo(threadInfo)+"\n");
			}
		}
		return(sb.toString());
		
	}
}
