package ru.practicum.shareit.initializer;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public class JpaProxyInitializer {

    public static <T> T initialize(T entity) {
        if (entity == null) {
            return null;
        }
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
