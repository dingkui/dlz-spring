package com.dlz.comm.util.tree;

import com.dlz.comm.json.JSONMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树结构工具类
 * 
 * 提供将节点数组转换为树形结构的功能
 *
 * @author dk
 * @since 2023
 */
public class TreeUtil {
	/**
	 * 将节点列表归并为一个森林（多棵树）（填充节点的children域）
	 * 时间复杂度为O(n^2)
	 *
	 * @param items 节点列表
	 * @param <IdType> 节点ID类型
	 * @param <T> 节点类型，需实现ITree接口
	 * @return 多棵树的根节点集合
	 */
	public static <IdType, T extends ITree<IdType, T>> List<T> toTree(List<T> items) {
		//森林的所有节点
		final Map<IdType, T> nodeMap = items.stream().collect(Collectors.toMap(ITree::getId, node -> node));
		//森林的父节点ID
		final Set<IdType> parentIds = new HashSet<>();

		items.forEach(tree -> {
			if (!tree.isRoot()) {
				ITree<IdType, T> parent = nodeMap.get(tree.getParentId());
				if (parent != null) {
					parent.getChildren().add(tree);
				} else {
					parentIds.add(tree.getId());
				}
			}
		});
		return items.stream()
				.filter(item -> item.isRoot() || parentIds.contains(item.getId()))
				.collect(Collectors.toList());
	}


	/**
	 * 将节点列表归并为一个森林（多棵树）（填充节点的children域）
	 * 时间复杂度为O(n^2)
	 *
	 * @param items 节点列表
	 * @param keyId ID字段名
	 * @param keyPid 父ID字段名
	 * @return 多棵树的根节点集合
	 */
	public static List<JSONMap> toTree(List<?> items, String keyId, String keyPid) {
		return toTree(items, keyId, keyPid, false);
	}

    /**
     * 将节点列表归并为一个森林（多棵树）（填充节点的children域）
     * 时间复杂度为O(n^2)
     *
     * @param items 节点列表
     * @param keyId 取得ID的函数 如：T::getId 或 ()->item.getStr("id")
     * @param keyPid 取得父ID的函数 如：T::getPid 或 ()->item.getStr("parentId")
     * @param children 取得子节点的函数   如：(item)->item.getList("children",T.class)或T::getChildren()
     * @param mkParent 创建父节点的函数 不为空时用此方法创建1级节点，为空时找不到父节点则设为1级节点   如：(id)->new JSONMap("id",id),()->new T()
     * @return 多棵树的根节点集合
     */
    public static <T,IdType> List<T> toTree(List<T> items,
                                            Function<T,IdType> keyId,
                                            Function<T,IdType> keyPid,
                                            Function<T,List<T>> children,
                                            Function<IdType,T> mkParent) {
        Map<IdType, T> nodeMap = items.stream().collect(Collectors.toMap(item -> keyId.apply( item), item -> item));

        List<T> re = new ArrayList<>();
        if (mkParent!=null) {
            items.forEach(forest -> {
                IdType pid = keyPid.apply(forest);
                if ("".equals(pid) || "0".equals(pid)) {
                    re.add(forest);
                } else {
                    T pnode = nodeMap.get(pid);
                    if (pnode == null) {
                        pnode = mkParent.apply(pid);
                        nodeMap.put(pid, pnode);
                        re.add(pnode);
                    }
                    children.apply(pnode).add(forest);
                }
            });
        } else {
            items.forEach(forest -> {
                IdType pid = keyPid.apply(forest);
                T pnode = nodeMap.get(pid);
                if (pnode != null) {
                    children.apply(pnode).add(forest);
                } else {
                    re.add(forest);
                }
            });
        }
        return re;
    }
	/**
	 * 将节点列表归并为一个森林（多棵树）（填充节点的children域）
	 * 时间复杂度为O(n^2)
	 *
	 * @param items 节点列表
	 * @param keyId ID字段名
	 * @param keyPid 父ID字段名
	 * @param mkParent 是否创建父节点
	 * @return 多棵树的根节点集合
	 */
	public static List<JSONMap> toTree(List<?> items, String keyId, String keyPid, boolean mkParent) {
		List<JSONMap> jsonMapStream = items.stream().map(item -> new JSONMap(item)).collect(Collectors.toList());
		Map<String, JSONMap> nodeMap = jsonMapStream.stream().collect(Collectors.toMap(item -> item.getStr(keyId), item -> item));

		List<JSONMap> re = new ArrayList<>();
		if (mkParent) {
			jsonMapStream.forEach(forest -> {
				String pid = forest.getStr(keyPid, "");
				if ("".equals(pid) || "0".equals(pid)) {
					re.add(forest);
				} else {
					JSONMap pnode = nodeMap.get(pid);
					if (pnode == null) {
						pnode = new JSONMap(keyId, pid);
						nodeMap.put(pid, pnode);
						re.add(pnode);
					}
					pnode.add("children", forest);
				}
			});
		} else {
			jsonMapStream.forEach(forest -> {
				String pid = forest.getStr(keyPid);
				JSONMap pnode = nodeMap.get(pid);
				if (pnode != null) {
					pnode.add("children", forest);
				} else {
					re.add(forest);
				}
			});
		}
		return re;
	}
}