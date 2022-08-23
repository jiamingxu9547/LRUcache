package edu.njit.cs114;



/**
 * Author: Ravi Varadarajan
 * Date created: 6/16/20
 */
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class LRUCache<K,T extends Cacheable<K>> {

    // list in LRU order
    private final List<K> lruList;
    int size;
	private Persister< K, T> persister;

    int accessedTime;
    int accessedNeedRetrieved;
    /**
     *
     * Iterator class only for only keys of cached items; order should be in LRU order, most recently used first
     *
     * @param <K>
     */
    private class CacheKeyIterator<K> implements Iterator<K> {
    	private int index;

    	
        @Override
        public boolean hasNext() {
            return index < lruList.size();
        }
        @Override
        public K next() {
            if (index < lruList.size()) {
                return (K) lruList.get(index++);
            } else {
                return null;
            }
        }

    }
    

    /**
     * Constructor
     *
     * @param size      initial size of the cache which can change later
     * @param persister persister instance to use for accessing/modifying evicted items
     */
    
	public LRUCache(int size, Persister<? extends K, ? extends T> persister) {
        lruList = new LinkedList<>();
        this.size = size;
        this.persister = (Persister<K, T>) persister;
        this.accessedTime=0;
        this.accessedNeedRetrieved=0;
        
        /** add more code if necessary */
    }

    /**
     * Modify the cache size
     *
     * @param newSize
     */
    public void modifySize(int newSize) {
    	this.size = newSize;
    }

    /**
     * Get item with the key (need to get item even if evicted)
     *
     * @param key
     * @return
     */
    public T getItem(K key) { 
    	T item = persister.getItem(key);
    	if(item != null) {//item exists
    		accessedTime +=1;
    		
    		if(!lruList.contains(key)) {//key not in cache 
    			accessedNeedRetrieved +=1;
    			if(lruList.size()+1>size) {//cache is full
    				lruList.remove(lruList.size()-1);
    			}
    		}
    		else {//key in cache
        		lruList.remove(key);//remove the key
    		}
    		lruList.add(0,key);//add the key
    	}
        return item;
    }

    /**
     * Add/Modify item with the key
     *
     * @param item item to be put
     */
    public void putItem(T item) {
    	
    	K key = item.getKey();//find  the key
    	if(getItem(key)==null) {// this key is new both in persister and cache.if not,this key has already been moved to the head to the cache in GetItem().
    		if(lruList.size()+1>size) {//cache is full 
    			lruList.remove(lruList.size()-1);
    		}
    		lruList.add(0,key);//add into cache  
    	}      
    	persister.persistItem(item);;//add into persister
    }

    /**
     * Remove an item with the key from cache as well as from persistent store
     *
     * @param key
     * @return item removed or null if it does not exist
     */
    public T removeItem(K key) {
    	
    	lruList.remove(key);//remove from cache    	
        return persister.removeItem(key);//remove from persister and return the item
    }

    /**
     * Get cache keys
     *
     * @return
     */
    public Iterator<K> getCacheKeys() {

        return new CacheKeyIterator<>();
    }

    /**
     * Get fault rate (proportion of accesses (only for retrievals and modifications) not in cache)
     *
     * @return
     */
    public double getFaultRatePercent() {
    	double result = 100*accessedNeedRetrieved/accessedTime;
        return result;
    }

    /**
     * Reset fault rate stats counters
     */
    public void resetFaultRateStats() {
        this.accessedTime =0;
        this.accessedNeedRetrieved = 0;
    	
    }

    public static void main(String[] args) {
        LRUCache<String, SimpleCacheItem> cache = new LRUCache<>(20, new SimpleFakePersister<>());
        for (int i = 0; i < 100; i++) {
            cache.putItem(new SimpleCacheItem("name" + i, (int) (Math.random() * 200000)));
            String name = "name" + (int) (Math.random() * i);
            SimpleCacheItem cacheItem = cache.getItem(name);
            if (cacheItem != null) {
                System.out.println("Salary for " + name + "=" + cacheItem.getAnnualSalary());
            }
            cache.putItem(new SimpleCacheItem("name" + (int) (Math.random() * i), (int) (Math.random() * 200000)));
            name = "name" + (int) (Math.random() * i);
            //cache.removeItem(name);
            System.out.println("Fault rate percent=" + cache.getFaultRatePercent());
        }
        Iterator<String> iter = cache.getCacheKeys();
        while (iter.hasNext()) {
            System.out.println("Key : "+iter.next());
        }
        for (int i = 0; i < 100; i++) {
            String name = "name" + (int) (Math.random() * 100);
            SimpleCacheItem cacheItem = cache.getItem(name);
            if (cacheItem != null) {
                System.out.println("Salary for " + name + "=" + cacheItem.getAnnualSalary());
            }
            System.out.println("Fault rate percent=" + cache.getFaultRatePercent());
        }
        for (int i = 0; i < 30; i++) {
            String name = "name" + (int) (Math.random() * i);
            cache.removeItem(name);
        }
        cache.resetFaultRateStats();
        cache.modifySize(50);
        for (int i = 0; i < 100; i++) {
            cache.putItem(new SimpleCacheItem("name" + i, (int) (Math.random() * 200000)));
            String name = "name" + (int) (Math.random() * i);
            SimpleCacheItem cacheItem = cache.getItem(name);
            if (cacheItem != null) {
                System.out.println("Salary for " + name + "=" + cacheItem.getAnnualSalary());
            }
            cache.putItem(new SimpleCacheItem("name" + (int) (Math.random() * i), (int) (Math.random() * 200000)));
            name = "name" + (int) (Math.random() * i);
            cache.removeItem(name);
            System.out.println("Fault rate percent=" + cache.getFaultRatePercent());
        }
    }

}




