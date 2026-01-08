import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  OneToMany,
} from 'typeorm';
import { DocMeta } from './DocMeta';
import { DocControl } from './DocControl';

@Entity('sys_user')
export class SysUser {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column({ unique: true })
  username!: string;

  @Column()
  password!: string;

  @Column({ unique: true })
  email!: string;

  @Column({ nullable: true })
  nickname?: string;

  @Column({ nullable: true })
  avatar?: string;

  @CreateDateColumn()
  createdAt!: Date;

  @UpdateDateColumn()
  updatedAt!: Date;

  // 关联关系
  @OneToMany(() => DocMeta, (doc) => doc.createdBy)
  createdDocs!: DocMeta[];

  @OneToMany(() => DocMeta, (doc) => doc.owner)
  ownedDocs!: DocMeta[];

  @OneToMany(() => DocControl, (control) => control.user)
  controls!: DocControl[];
}

