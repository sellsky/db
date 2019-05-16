package tk.bolovsrol.db.orm.refgen;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

interface ObjectGenerator {

    /**
     * @return суффикс имени генерируемой сущности
     */
    String getSuffix();

    /**
     * @return true, если существующую сущность надо заменять, или false, если надо оставить
     */
    boolean isOverrideExisting();

    /**
     * @param metaInterface интерфейс, для которого надо генерировать сущность
     * @return сгенерированное тельце сущности
     */
    String generate(MetaInterface metaInterface) throws UnexpectedBehaviourException;

}
