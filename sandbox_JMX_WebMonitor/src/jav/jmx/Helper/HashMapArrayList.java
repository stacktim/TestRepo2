package jav.jmx.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
public class HashMapArrayList<K, V>{

    private HashMap<K, ArrayList<V>> map = new HashMap<K, ArrayList<V>>();
    
    public void AddToMap(K iStr, V ob){
    	if( map.containsKey(iStr)==false){
    		map.put(iStr, new ArrayList<V>());
    	}

    	ArrayList<V> al =  map.get(iStr);
		al.add(ob);
    }
    
	public Set<K> getKeys(){
		return map.keySet();
	}
	
	public ArrayList<V> getValue(K iStr){
		if (map.containsKey(iStr)==false){
			return null;
		}
		return map.get(iStr);		
	}
    public String HMtoString(){
    	StringBuffer sb = new StringBuffer();
        for (Iterator<K> iterator = map.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            sb.append( "\n'" +  key + "\t"+ map.get(key) );
        }
        return sb.toString();
    }
}