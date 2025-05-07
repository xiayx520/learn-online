// kev value 数据
export interface IKVData {
	code: string;
	desc: string;
}

export interface IWorkVO {
	id?: number;
	title: string;
	description: string;
	bindCourses?: string;
	userNum?: number;
	status?: string;
	createUser?: number;
	createDate?: string;
	changeDate?: string;
}

export interface IWorkDTO extends IWorkVO {
	id: number;
	bindCourses: string;
	userNum: number;
	status: string;
	createUser: number;
	createDate: string;
	changeDate: string;
}
