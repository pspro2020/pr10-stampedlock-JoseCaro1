package ejercicio9;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class Almacen {


    private final List<Producto> productos;
    private List<Integer> productosId = new ArrayList<>();
    private final StampedLock stampedLock = new StampedLock();

    Random random = new Random();

    public Almacen(List<Producto> productos) {
        this.productos = productos;
    }

    protected int consultarStock(int productId) {
        int count = 0;
        long stamp = stampedLock.tryOptimisticRead();
        for (Integer id : productosId) {
            if (id.equals(productId)) {
                count++;
            }
        }
        if (!stampedLock.validate(stamp)) {
            stampedLock.readLock();
            try {
                for (Integer id2 : productosId) {
                    if (id2.equals(productId)) {
                        count++;
                    }
                }
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        System.out.printf("Hay %d del %s en stock\n", count, Thread.currentThread().getName());
        return count;
    }

    protected int añadirStock() {
        Producto productoAdd = productos.get(random.nextInt(productos.size()));
        long stamp = stampedLock.writeLock();
        try {
            productosId.add(productoAdd.getId());
        } finally {
            stampedLock.unlockWrite(stamp);

        }
        System.out.printf("Se ha añadido el producto con el id %d\n", productoAdd.getId());
        return productoAdd.getId();


    }

}
