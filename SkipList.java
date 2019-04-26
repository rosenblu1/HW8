import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int INITIAL_HEIGHT = 16;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  ArrayList<SLNode<K, V>> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the skiplist.
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;
  
  /**
   * counter for operational testing
   */
  int counter = 0;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      front.add(null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = INITIAL_HEIGHT;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */
  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()


  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  @Override
  public V set(K key, V value) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    SLNode<K, V> node = new SLNode<K, V>(null, null, this.height);
    node.next = this.front;

    // populate update
    SLNode<K, V>[] update = (SLNode<K, V>[]) new SLNode[this.height];

    for (int i = height - 1; i >= 0; i--) {
      while (node.next(i) != null && this.comparator.compare(node.next(i).key, key) < 0) {
        node = node.next(i);
      } // while
      update[i] = node;
    } // for
    node = node.next(0);

    // if key already exists in list
    if (node != null && comparator.compare(node.key, key) == 0) {
      V tmp = node.value;
      node.value = value;
      return tmp;
    }
    // inserting new node
    int newHeight = randomHeight();
    SLNode<K, V> newNode = new SLNode<K, V>(key, value, newHeight);

    if (newHeight > this.height) {
      //make dummy
      SLNode<K, V> header = new SLNode<>(null, null, this.height); 
      header.next = this.front;

      SLNode<K, V>[] newUp = (SLNode<K, V>[]) new SLNode[newHeight];
      for (int i = 0; i < this.height; i++) {
        newUp[i] = update[i];
      }

      for (int i = this.height; i < newHeight; i++) {
        newUp[i] = header;
        front.add(null);
      }
      update = newUp;

      this.height = newHeight;
    } // if newHeight > height

    for (int i = 0; i < newHeight; i++) {
      newNode.next.set(i, update[i].next(i));
      update[i].next.set(i, newNode);
    } // for

    this.size++;
    return null;
  } // set(K,V)



  @Override
  public /**
          * returns the node right in front of where searchKey is
          */
  V get(K key) {

    SLNode<K, V> node = new SLNode<>(null, null, this.height);
    node.next = this.front;

    for (int i = height - 1; i >= 0; i--) {
      // invariant: node.key <= key (horizontal)
      while (node.next(i) != null && comparator.compare(node.next(i).key, key) < 0) {
        node = node.next(i);
      } // while
    } // for
    node = node.next(0);

    if (node != null && comparator.compare(node.key, key) == 0) {
      return node.value;
    }

    throw new IndexOutOfBoundsException();


  } // get(K,V)

  @Override
  public int size() {
    return this.size;
  } // size()

  @Override
  public boolean containsKey(K key) {
    try {
      get(key);
    } catch (Exception IndexOutOfBoundsException) {
      return false;
    }
    return true;
  } // containsKey(K)

  @Override
  public V remove(K key) {
    if (key == null)
      throw new NullPointerException("null key");

    SLNode<K, V> node = new SLNode<>(null, null, this.height);
    node.next = this.front;

    ArrayList<SLNode<K, V>> update = new ArrayList<>(height);
    for (int i = 0; i < height; i++) {
      update.add(null);
    }

    for (int i = this.height - 1; i >= 0; i--) {
      while (node.next(i) != null && this.comparator.compare(node.next(i).key, key) < 0) {
        node = node.next(i);
      }
      update.set(i, node);
    } // for
    node = node.next(0);

    if (node != null && this.comparator.compare(node.key, key) == 0) {
      for (int i = 0; i < this.height; i++) {
        if (update.get(i).next(i) != node) {
          break;
        }
        update.get(i).next.set(i, node.next(i));
      } // for

      while (this.height > 0 && this.front.get(this.height - 1) == null) {
        this.height--;
      }

      this.size -= 1;
      return node.value;
    } // if

    return null;
  } // remove(K)

  @Override
  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public K next() {
        return nit.next().key;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  @Override
  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public V next() {
        return nit.next().value;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    SLNode<K, V> node = new SLNode<K, V>(null, null, this.height);
    node.next = this.front;

    node = node.next(0);
    while (node != null) {
      action.accept(node.key, node.value);
      node = node.next(0);
    } // while

  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    // Forthcoming
  } // dump(PrintWriter)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    }
    return result;
  } // randomHeight()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.get(0);

      @Override
      public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override
      public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next(0);
        return temp;
      } // next();
    }; // new Iterator
  } // nodes()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

 // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n);
    for (int i = 0; i < n; i++) {
      this.next.add(null);
    } // for
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Get the next node at the specified level.
   */
  public SLNode<K, V> next(int level) {
    SkipList.this.counter += 1;
    return this.next.get(level);
  } // next

  /**
   * Set the next node at the specified level.
   */
  public void setNext(int level, SLNode<K, V> next) {
    SkipList.this.counter += 1;
    this.next.set(level, next);
    
  } // setNext(int, SLNode<K,V>)

} // SLNode<K,V>
} // SkipList
