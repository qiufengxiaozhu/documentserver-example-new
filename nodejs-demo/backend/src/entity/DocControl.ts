import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  JoinColumn,
  Unique,
} from 'typeorm';
import { SysUser } from './SysUser';
import { DocMeta } from './DocMeta';

@Entity('doc_control')
@Unique(['userId', 'docId'])
export class DocControl {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column()
  userId!: string;

  @Column()
  docId!: string;

  // 权限控制
  @Column({ default: true })
  canEdit!: boolean;

  @Column({ default: true })
  canDownload!: boolean;

  @Column({ default: true })
  canPrint!: boolean;

  @Column({ default: true })
  canCopy!: boolean;

  @Column({ default: true })
  canComment!: boolean;

  @Column({ default: true })
  canShare!: boolean;

  // 水印配置
  @Column({ default: false })
  watermarkEnabled!: boolean;

  @Column({ nullable: true })
  watermarkText?: string;

  @Column({ default: 'text', nullable: true })
  watermarkType?: string;

  // 扩展配置
  @Column({ type: 'text', nullable: true })
  extensions?: string; // JSON string

  @CreateDateColumn()
  createdAt!: Date;

  @UpdateDateColumn()
  updatedAt!: Date;

  // 关联关系
  @ManyToOne(() => SysUser, (user) => user.controls)
  @JoinColumn({ name: 'userId' })
  user!: SysUser;

  @ManyToOne(() => DocMeta, (doc) => doc.controls)
  @JoinColumn({ name: 'docId' })
  doc!: DocMeta;
}

