                                
HashMap 介绍
==
1、概述
--
   ####  基于哈希表的Map接口的非同步实现，以key-value存储形式存在。主要有以下几个特性：
   + HashMap中的key和value都允许为null，但最多只有一个为null的key
   + HashMap不保证有序（比如插入的顺序，不同时间迭代同一个HashMap的排序也可能不同）
   + HashMap是非同步的，线程不安全 
       
2、HashMap的原理
--
  HashMap容器，实质上还是一个哈希数组结构，所以在元素插入的时候，可能就会发生hash冲突的情况，对于这种情况，解决方式有两种，一种是开放地址式，当发生hash冲突的时候，就继续寻找，直到找到没有冲突的hash值；一种是拉链式，将冲突的元素放入链表。HashMap采用的就是拉链式。（hash的主要作用是尽量保持元素的分散性，防止出现hash碰撞）
- JDK1.8之前 HashMap主要由数组+链表组成。数组是HashMap的主体，链表则是用来解决hash冲突的，冲突的元素会被放入链表中。原理图如下：
![alt text](https://wx2.sinaimg.cn/mw690/a84a0ae4ly1gf5pb52oohj20qs0ls768.jpg "jdk1.7") 
- JDK1.8以后 HashMap主要由数组+链表+红黑树组成。当链表长度超过阈值（默认为8）的时候，就会将链表转换为红黑树（将链表转换成红黑树前会判断，如果当前数组的长度小于64，那么会选择先进行数组扩容，而不是转换为红黑树）。原理图如下：
![alt text](https://wx2.sinaimg.cn/mw690/a84a0ae4ly1gf5pb535jgj20si0qk769.jpg "jdk1.8")

3、源码解析
--
### 3.1、类属性
<pre>
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable {
    // 序列号
    private static final long serialVersionUID = 362498820763181265L;    
    // 默认的初始容量是16
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;   
    // 最大容量
    static final int MAXIMUM_CAPACITY = 1 << 30; 
    // 默认的填充因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    // 当桶(bucket)上的节点数大于这个值时会转成红黑树
    static final int TREEIFY_THRESHOLD = 8; 
    // 当桶(bucket)上的节点数小于这个值时树转链表
    static final int UNTREEIFY_THRESHOLD = 6;
    /*  当桶中的bin被树化时最小的hash表容量。
     *  如果没有达到这个阈值，即hash表容量小于MIN_TREEIFY_CAPACITY，当桶中bin的数量太多时会执行resize扩容操作。
     *  这个MIN_TREEIFY_CAPACITY的值至少是TREEIFY_THRESHOLD的4倍。
     */    
    static final int MIN_TREEIFY_CAPACITY = 64;
    // 存储元素的数组，总是2的幂次倍
    transient Node<k,v>[] table; 
    // 存放具体元素的集
    transient Set<map.entry<k,v>> entrySet;
    // 存放元素的个数，注意这个不等于数组的长度。
    transient int size;
    /**
     * 结构性变更的次数。
     * 结构性变更是指map的元素数量的变化，比如rehash操作。
     * 用于HashMap快速失败操作，比如在遍历时发生了结构性变更，就会抛出ConcurrentModificationException。
     */   
    transient int modCount;   
    // 临界值 当实际大小(容量*填充因子)超过临界值时，会进行扩容
    int threshold;
    // 填充因子,resize后容量的大小会增加现有size * loadFactor
    final float loadFactor;
}
</pre>
关键参数：
 - DEFAULT_INITIAL_CAPACITY: 默认初始容量是16，每次扩展或者手动初始化时，长度必须是2的幂。之所以选择16，是为了服务于从Key映射到index的Hash算法。
 - threshold: threshold = capacity * Load factor，表示容器所能容纳的 key-value 对极限，超过了就该扩容了。
 - loadFactor: 用来衡量数组存放数据的疏密程度，loadFactor越趋近于1，数组存放的数据也就越密，存放数据多但查询效率低；loadFactor越趋近于0，则数组存放的数据也就越稀疏，查询效率高但数据存放很分散，数组利用率低。
   loadFactor默认是0.75，是对空间和时间效率的一个平衡选择，虽然可以在初始化的时候进行修改，但除非特殊的情况下才考虑修改，这个值可大于1。
 - modCount: 记录修改次数。
 - size: 表示实际存在的键值对数量，在调用putValue()方法以及removeNode()方法时，都会对其造成改变，和capacity区分一下即可。
 - table: 一个哈希桶数组，用于存储添加到HashMap中的Key-value对，是一个Node数组，Node是一个静态内部类，一种数组和链表相结合的复合结构。
 
### 3.2、初始化
<pre>
    // 默认构造函数
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; 
     }
     
     // 另一个“Map”做入参的构造函数
     public HashMap(Map<? extends K, ? extends V> m) {
         this.loadFactor = DEFAULT_LOAD_FACTOR;
         putMapEntries(m, false);
     }
     
     // 指定容量大小的构造函数，可减少扩容的开销
     public HashMap(int initialCapacity) {
         this(initialCapacity, DEFAULT_LOAD_FACTOR);
     }
    
     // 指定容量大小和加载因子的构造函数
     public HashMap(int initialCapacity, float loadFactor) {
         if (initialCapacity < 0)
             throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
         if (initialCapacity > MAXIMUM_CAPACITY)
             initialCapacity = MAXIMUM_CAPACITY;
         if (loadFactor <= 0 || Float.isNaN(loadFactor))
             throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
         this.loadFactor = loadFactor;
          //tableSizeFor()返回大于或等于最接近输入参数的2的整数次幂的数，比如initialCapacity = 7，那么转化后就是8。
         this.threshold = tableSizeFor(initialCapacity);
     }</pre>
 <pre>
 final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
     int s = m.size();
     if (s > 0) {
         // 判断table是否已经初始化
         if (table == null) { 
             // 计算map的容量，键值对的数量 = 容量 * 填充因子
             float ft = ((float)s / loadFactor) + 1.0F;
             int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                     (int)ft : MAXIMUM_CAPACITY);
             // 计算得到的t大于阈值，则初始化阈值
             if (t > threshold)
                 threshold = tableSizeFor(t);
         }
         // 已初始化，并且m元素个数大于阈值，进行扩容处理
         else if (s > threshold)
             resize();
         // 将m中的所有元素添加至HashMap中
         for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
             K key = e.getKey();
             V value = e.getValue();
             putVal(hash(key), key, value, false, evict);
         }
     }
 }</pre>
 <pre>
 static final int tableSizeFor(int cap) {
   int n = cap - 1;
   // >>>: 无符号右移，空位补0
   |: 或运算
   n |= n >>> 1;
   n |= n >>> 2;
   n |= n >>> 4;
   n |= n >>> 8;
   n |= n >>> 16;
   return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
 }</pre>
 #### tableSizeFor的目的是把入参从n位开始到最低位都至1，得到的数肯定是大于或者等于原来的值且为奇数，+1之后就能得到大于且最接近入参的2的整数次幂值。
 - 为什么右移是16位，因为int类型为32位，移动16位就已经完全计算完了。
 - 为什么n=cap-1，因为如果是8，01000，移动4位后就是01111，再+1就是16了，显然就不是最近的2次幂了，所以先减1，就是为了防止出现cap正好为2的整数次幂。

### 3.3、HashMap方法介绍
#### 3.3.1、put方法
<pre>
public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
}</pre>
#### put方法之前我们先了解一下HashMap是如何确定索引的位置
##### 前面已经介绍过HashMap的数据结构是数组和链表的结合，所以元素的位置分布我们还是希望尽量均匀些，尽量使每个位置上只有一个元素，这样我们就可以直接通过hash值找到对应位置的元素，就不需要去遍历链表，可大大优化查询效率。
源码如下：
<pre>
    /**获取hash值方法*/
    static final int hash(Object key) {
        int h;
        // key.hashCode(): 返回hashCode值
        // >>>: 无符号右移，空位补0
        // ^: 按位异或
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    
    /**获取数组下标方法*/
    static int indexFor(int h, int length) {
        //jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
         return h & (length-1);取模运算
    }
</pre>
- 第一步获取哈希值，key.hashCode()函数调用的是key键值类型自带的哈希函数，返回int型散列值，然后拿高16位异或低16位，这么做的原因是考虑到后续扩容确定数组下标的时候，需使用hash值对数组table的length的长度取模，如果table的length较小时，高位bit参与不到计算中，所以为了将高位bit也加入计算，就采取这种高16、低16异或，同时不会有太大的开销。

- 第二步获取数组下标，因为key获取hash值是使用的自带哈希函数，返回的值是int型散列值，如果直接拿它做下标的话，考虑到它的大小为‑2147483648 到 2147483648，前后加起来40亿的映射空间，HashMap的初始容量才16，所以是不能直接拿来用的。所以得进行对数组长度的取模运算，得到的余数才能作为下标。1.7中是在indexFor()这个函数里完成的，1.8没有这个方法，但是获取原理一样。
这里可能有人会疑问，为什么要和table的length-1进行与运算，为什么不直接和length与运算，是因为length的大小是2的整次幂，减1之后就相当于它的低位掩码，hash值与运算后，就只会保留低位值，高位全部归0，得到的值就能作为数组下标了。但如果每次只取低位进行运算又可能会导致碰撞会很严重，离散如果做得不好，分布上出现等差数列的漏洞，可能就会出现低位规律性重复性的问题。这里就可以解释一下为什么HashMap的数组长度要取2的整次幂，举个例子，假设数组长度分别为15和16，两个hash值分别是8和9，那么8&(15-1)=0100，9&(15-1)=0100；8&(16-1)=0100,9&(16-1)=0101。所以当它们与（15-1）进行与运算后，产生了相同的结果，它们会被定位到同一条链上，那么在查询的时候，就需要遍历这条链，降低了查询效率。同样我们会发现，hash值与(15-1)进行y与运算后，最后一位永远都是0，那么0001，0011，0101，1001，1011，0111，1101这几个数就永远取不到了，空间浪费严重，数组的可用长度小了很多，且降低了查询效率，所以HashMap数组取2的整次幂，主要是从避免哈希碰撞考虑的。另外取模使用&而不是%，主要是从效率这块考虑的，&比%有更高的效率。

##### 再了解一下node节点
 <pre>
  // Node 
 static class Node<K,V> implements Map.Entry<K,V> {
     // 哈希值，存放元素到hashmap中时用来与其他元素hash值比较
         final int hash;
         final K key;
         V value;
        // 指向下一个节点
         Node<K,V> next;
     }
     
 // TreeNode 红黑树
 static final class TreeNode<k,v> extends LinkedHashMap.Entry<k,v> {
     TreeNode<k,v> parent;  //父节点
     TreeNode<k,v> left; //左子树
     TreeNode<k,v> right;//右子树
     TreeNode<k,v> prev;    //上一个同级节点
     boolean red;    //颜色属性
 }
 </pre>
- 解释一下:拿咱们坐火车来类比，我们用身份证（key）去买票，通过身份证（key）在12306上预订车票（对key进行hash运算），拿到火车票（生成的hash值）我们就能知道自己是哪个车厢哪个座位，然后我们（就是value）找到座位，比如12车01A，我们就能知道12车01B就是下一个（next），这样我们就是一个node节点，整个车厢就是一个node数组，即一个table。

#### put方法执行详情
![alt text](https://wx4.sinaimg.cn/mw690/a84a0ae4ly1gf5ipnynrpj21090u0gwa.jpg "put")
源码及解释如下：
<pre>
    //put方法
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
    
    //插入元素
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        //如果table没有初始化，或者初始化大小为0，则进行resize()操作
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        //如果hash值对应下标的tab[i]==null，则直接将生成新节点并放入桶中  
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        //如果hash值对应下标的tab[i]在桶内存在，则解决完冲突后再放入桶中    
        else {
            Node<K,V> e; K k;
            //如果tab[i]的首个元素与传入的元素一致（hash相同，并且key相同），放入临时node节点e中
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            //如果tab[i]的首个元素与传入的元素不一致，判断其是否为treeNode，如果是则放入树中    
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            //不一致，且不是treeNode，则将其放入链表中
            else {
                //遍历tab[i]，判断链表长度是否大于8
                for (int binCount = 0; ; ++binCount) {
                    //tab[i]下个节点为null，则在尾部插入新节点
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        //如果链表的长度大于树的阈值，将存储元素的数据结构变更为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    //如果查到已存在key，停止遍历
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    //用于遍历链表，与前面的e=p.next()配合    
                    p = e;
                }
            }
            //表示在桶中找到了key值、hash值相同的节点
            if (e != null) { // existing mapping for key
                //记录e的value
                V oldValue = e.value;
                //如果onlyIfAbsent为false或者旧值为null，则直接用新值替换旧值
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                //访问后回调    
                afterNodeAccess(e);
                return oldValue;
            }
        }
        //结构性修改
        ++modCount;
        //如果kv数量大于阈值，进行扩容
        if (++size > threshold)
            resize();    
        afterNodeInsertion(evict);
        return null;
    }
</pre>
put(K key, V value)方法是将指定的key, value对添加到map里。该方法首先会对map做一次查找，看是否包含该K，如果已经包含则直接返回；如果没有找到，则将元素插入容器：
##### 1、判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容；
##### 2、根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加；
##### 3、当table[i]不为空，判断table[i]的首个元素是否和传入的key一样，如果相同直接覆盖value；
##### 4、判断table[i]是否为treeNode，即table[i]是否是红黑树，如果是红黑树，则直接在树中插入键值对；
##### 5、遍历table[i]，判断链表长度是否大于8，大于8的话把链表转换为红黑树，在红黑树中执行插入操作，否则进行链表的插入操作；遍历过程中若发现 key已经存在直接覆盖value即可；
##### 6、插入成功后，判断实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容操作；

#### 3.3.2、resize方法
#### 扩容机制
HashMap是以最小性能来完成扩容，每次扩容都是前一次size()的2倍，初始容量是16，扩容后，元素的位置要么在原位置，要么在原来的位置+扩容前size()的位置。如果扩容前容量为16，其中一个元素扩容前下标是5，那么扩容后的位置要么还是5，要么是5+16。
![alt text](https://wx3.sinaimg.cn/mw690/a84a0ae4ly1gf5ipny2sgj20xs0ts7b1.jpg "resize")
源码及解释如下：
<pre>
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        //扩容前的容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        //旧的阈值
        int oldThr = threshold;
        //新的容量、阈值初始化为0
        int newCap, newThr = 0;
        if (oldCap > 0) {
             //如果旧的容量已经达到了最大的容量，则将阈值设为最大，随机去碰撞
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
             //如果两倍的旧容量小于最大的容量且旧容量大于等于默认初始化容量，则容量扩大两倍，阈值也扩大两倍。
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        //如果旧容量为0且旧阈值大于0，则赋值给新的容量(针对初始化的时候指定了其容量的构造函数的这种情况)
        else if (oldThr > 0) 
            newCap = oldThr;
        //调用无参数的构造函数的情况
        else {               
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
          // 新阈值为0，则通过：新容量*填充因子 来计算
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        //根据新的容量来初始化table，并赋值给table
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
         //如果旧的table里面有存放节点，说明是扩容而不是初始化，则将旧的赋值给新的table
        if (oldTab != null) {
            // 把每个bucket都移动到新的buckets中
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                  //将下标为j的数组赋给临时节点e
                if ((e = oldTab[j]) != null) {
                     //清空
                    oldTab[j] = null;
                     //如果该节点没有指向下一个节点，则直接通过计算hash和新的容量来确定新的下标，并指向e
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    //如果为树节点，按照树节点的来拆分
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    //如果是链表形式，将该桶拆分成两份(不一定均分)
                    else { 
                        //loHead是拆分后的，链表的头部，tail为尾部
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            //根据e的hash值和旧的容量做位与运算是否为0来拆分，注意之前是 e.hash & (oldCap - 1)
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            // 原索引+oldCap
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 低hash值的链表放入数组的原始位置
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                         // 原索引+oldCap放到bucket里
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
</pre>
JDK7中，HashMap的内部数据保存的都是链表。因此逻辑相对简单：在准备好新的数组后，map会遍历数组的每个“桶”，然后遍历桶中的每个Entity，重新计算其hash值（也有可能不计算），找到新数组中的对应位置，以头插法插入新的链表。需要注意的点：
- 因为是头插法，因此新旧链表的元素位置会发生转置现象。
- 元素迁移的过程中在多线程情境下有可能会触发死循环（无限进行链表反转）。
JDK1.8中，不需要像JDK1.7实现那样重新计算hash值，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，因此，在扩容时，不需要重新计算元素的hash了，只需要判断最高位是1还是0就好了。需要注意的点：
- 迁移元素时是正序的，不会出现链表转置的发生。
- 如果某个桶内的元素超过8个，则会将链表转化成红黑树，加快数据查询效率。
- 为了性能在同一索引处发生哈希冲突到一定程度时，链表结构会转换为红黑数结构存储冲突元素，故在扩容时如果当前索引中元素结构是红黑树且元素个数小于链表还原阈值时就会把树形结构缩小或直接还原为链表结构
- resize()方法对整个数组以及桶进行了遍历，极其耗费性能。所以最好在初始化数组的时候就给默认容量
                                                                                           
#### 3.2.3、get方法
get(Object key)方法根据指定的key值返回对应的value，getNode(hash(key), key))得到相应的Node对象e，然后返回e.value。因此getNode()是算法的核心。
![alt text](https://wx3.sinaimg.cn/mw690/a84a0ae4ly1gf5ipnyefzj20sq0wy0yr.jpg "get")
源码及解释如下：
<pre>
/**
  * JDK1.8 get方法
  * 通过key获取参数值
  */
public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        //判断桶内是否有元素，第一个元素是否为null
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            //1、判断第一个元素是否与key匹配
            if (first.hash == hash &&
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                //2、判断链表是否红黑树结构
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                //3、如果不是红黑树结构，直接循环判断
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
}
</pre>
get 方法，首先通过hash()函数得到对应数组下标，然后依次判断。
##### 1、判断第一个元素与 key 是否匹配，如果匹配就返回参数值；
##### 2、判断链表是否红黑树，如果是红黑树，就进入红黑树方法获取参数值；
##### 3、如果不是红黑树结构，直接循环判断，直到获取参数为止；

#### 3.3.4、remove方法
remove(Object key)的作用是删除 key 值对应的 Node，该方法的具体逻辑是在removeNode(hash(key), key, null, false, true)里实现的。
![alt text](https://wx2.sinaimg.cn/mw690/a84a0ae4ly1gf5ipnycmlj20p0148gsu.jpg "remove")
<pre>
public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }
    

final Node<K,V> removeNode(int hash, Object key, Object value,boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        //如果 节点数组tab不为空、数组长度n大于0、根据hash定位到的节点对象p（该节点为树的根节点或链表的首节点）不为空，需要从该节点p向下遍历，找到那个和key匹配的节点对象   
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            // 如果当前节点的键和key相等，那么当前节点就是要删除的节点，赋值给node 
            if (p.hash == hash &&((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            //判断有无next节点
            else if ((e = p.next) != null) {
                //此节点红黑树，那么调用getTreeNode方法从树结构中查找满足条件的节点
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    //链接循环比较
                    do {
                    // 如果e节点的键是否和key相等，e节点就是要删除的节点，赋值给node变量，调出循环
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) 
                            node = e;
                            break;
                        }
                     //p保存当前遍历到的节点
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            //需要找的节点不为空
            if (node != null && (!matchValue || (v = node.value) == value ||(value != null && value.equals(v)))) {
                if (node instanceof TreeNode)   
            //在树中删除节点                                 
           ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                //如果要删除的是头节点
                else if (node == p)
                    tab[index] = node.next;
                else
                    //不是头节点，将当前节点指向删除节点的下一节点
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }
</pre>
jdk1.8 的删除逻辑实现比较复杂，相比 jdk1.7 而言，多了红黑树节点删除和调整：
##### 1、默认判断链表第一个元素是否是要删除的元素；
##### 2、如果第一个不是，就继续判断当前冲突链表是否是红黑树，如果是，就进入红黑树里面去找；
##### 3、如果当前冲突链表不是红黑树，就直接在链表中循环判断，直到找到为止；
##### 4、将找到的节点，删除掉，如果是红黑树结构，会进行颜色转换、左旋、右旋调整，直到满足红黑树特性为止；

### 3.4、Fail-Fast机制
我们知道java.util.HashMap不是线程安全的，因此如果在使用迭代器的过程中有其他线程修改了map，那么将抛出ConcurrentModificationException，这就是所谓fail-fast策略。这一策略在源码中的实现是通过modCount域，modCount顾名思义就是修改次数，对HashMap内容的修改都将增加这个值，那么在迭代器初始化过程中会将这个值赋给迭代器的expectedModCount。
<pre>
HashIterator() {  
    expectedModCount = modCount;  
    if (size > 0) { // advance to first entry  
    Entry[] t = table;  
    while (index < t.length && (next = t[index++]) == null)  
        ;  
    }  
} 
 
final Entry<K,V> nextEntry() {     
    if (modCount != expectedModCount)     
        throw new ConcurrentModificationException(); 
</pre>
##### 在迭代过程中，判断modCount跟expectedModCount是否相等，如果不相等就表示已经有其他线程修改了Map：
##### 注意到modCount声明为volatile，保证线程之间修改的可见性。


4、总结
--
##### 1、允许key和value为null，但最多只有一个为null的key；
##### 2、不能保证顺序；
##### 3、影响HashMap性能的两个变量：填充因子和初始化容量；通常来说，默认的填充因为0.75是一个时间和空间消耗的良好平衡。较高的填充因为减少了空间的消耗，但是增加了查找的时间；较低则减少了查找的时间，但空间但利用率低。
##### 4、最好能够在创建HashMap的时候指定其容量，这样能避免在插入的时候进行频繁的扩容，毕竟扩容本身就比较消耗性能；
##### 5、HashMap扩容是2的倍数。初始Map的时候无论传的容量是多少，只要不是2的N次幂，都会在内部处理为2的整次幂；
##### 6、hashmap是不同步的。如果要同步请使用Map m = Collections.synchronizedMap(new HashMap(...))或者ConcurrentHashMap；
##### 7、除了使用迭代器的remove方法，其他任何时间任何方式的修改，都会抛出ConcurrentModificationException，因此，面对并发的修改，迭代器很快就会完全失败，而不冒在将来不确定的时间发生任意不确定行为的风险；
##### 8、如果key是一个对象，记得在对象实体类里面，要重写equals和hashCode方法，不然在查询的时候，无法通过对象key来获取参数值；
##### 9、相比JDK1.7，JDK1.8引入红黑树设计，当链表长度大于8的时候，链表会转化为红黑树结构，发生冲突的链表如果很长，红黑树的实现很大程度优化了HashMap的性能，使查询效率比JDK1.7要快一倍；但大多数情况下，结构还是以桶的形式存在，检查是否存在树节点也会增加访问的时间。
