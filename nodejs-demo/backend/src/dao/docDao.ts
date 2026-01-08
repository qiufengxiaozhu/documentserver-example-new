import { AppDataSource } from '../database/DataSource';
import { DocMeta, DocControl } from '../entity';

const docRepository = AppDataSource.getRepository(DocMeta);
const controlRepository = AppDataSource.getRepository(DocControl);

/**
 * 创建文档元数据
 */
export async function createDoc(data: Partial<DocMeta>): Promise<DocMeta> {
  const doc = docRepository.create(data);
  await docRepository.save(doc);
  return docRepository.findOne({
    where: { id: doc.id },
    relations: ['createdBy', 'owner'],
  }) as Promise<DocMeta>;
}

/**
 * 根据ID查找文档
 */
export async function findById(id: string): Promise<DocMeta | null> {
  return docRepository.findOne({
    where: { id },
    relations: ['createdBy', 'owner'],
  });
}

/**
 * 根据名称查找文档
 */
export async function findByName(name: string): Promise<DocMeta | null> {
  return docRepository.findOne({
    where: { name, isDeleted: false },
  });
}

/**
 * 获取文档列表
 */
export async function findDocs(userId?: string): Promise<DocMeta[]> {
  const queryBuilder = docRepository
    .createQueryBuilder('doc')
    .leftJoinAndSelect('doc.createdBy', 'createdBy')
    .leftJoinAndSelect('doc.owner', 'owner')
    .where('doc.isDeleted = :isDeleted', { isDeleted: false });

  if (userId) {
    queryBuilder.andWhere(
      '(doc.createdById = :userId OR doc.ownerId = :userId OR doc.createdById = :share OR doc.ownerId = :share)',
      { userId, share: 'share' }
    );
  }

  return queryBuilder
    .orderBy('doc.modifiedAt', 'DESC', 'NULLS LAST')
    .addOrderBy('doc.createdAt', 'DESC')
    .getMany();
}

/**
 * 更新文档元数据
 */
export async function updateDoc(id: string, data: Partial<DocMeta>): Promise<void> {
  await docRepository.update(id, data);
}

/**
 * 获取文档控制配置
 */
export async function findControl(userId: string, docId: string): Promise<DocControl | null> {
  return controlRepository.findOne({ where: { userId, docId } });
}

/**
 * 根据ID查找文档控制配置
 */
export async function findControlById(id: string): Promise<DocControl | null> {
  return controlRepository.findOne({ where: { id } });
}

/**
 * 创建文档控制配置
 */
export async function createControl(data: Partial<DocControl>): Promise<DocControl> {
  const control = controlRepository.create(data);
  return controlRepository.save(control);
}

/**
 * 更新文档控制配置
 */
export async function updateControl(id: string, data: Partial<DocControl>): Promise<void> {
  await controlRepository.update(id, data);
}

