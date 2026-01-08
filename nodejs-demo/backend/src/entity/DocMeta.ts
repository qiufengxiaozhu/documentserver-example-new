import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  OneToMany,
  JoinColumn,
} from 'typeorm';
import { SysUser } from './SysUser';
import { DocControl } from './DocControl';

@Entity('doc_meta')
export class DocMeta {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column()
  name!: string;

  @Column({ default: '' })
  path!: string;

  @Column({ default: 0 })
  size!: number;

  @Column()
  extension!: string;

  @Column({ nullable: true })
  mimeType?: string;

  @Column({ default: 1 })
  version!: number;

  @Column({ default: false })
  isDeleted!: boolean;

  @CreateDateColumn()
  createdAt!: Date;

  @UpdateDateColumn()
  updatedAt!: Date;

  @Column({ type: 'datetime', nullable: true })
  modifiedAt?: Date;

  // 创建者信息
  @Column({ nullable: true })
  createdById?: string;

  @ManyToOne(() => SysUser, (user) => user.createdDocs, { nullable: true })
  @JoinColumn({ name: 'createdById' })
  createdBy?: SysUser;

  // 所有者信息
  @Column({ nullable: true })
  ownerId?: string;

  @ManyToOne(() => SysUser, (user) => user.ownedDocs, { nullable: true })
  @JoinColumn({ name: 'ownerId' })
  owner?: SysUser;

  // 关联关系
  @OneToMany(() => DocControl, (control) => control.doc)
  controls!: DocControl[];
}

