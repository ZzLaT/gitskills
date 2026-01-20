# 底部导航栏组件设计文档

## 概述

本文档详细说明了个人记账应用中底部导航栏组件的设计实现，包括结构化布局、交互状态、响应式行为等关键特性。

## 组件架构

### 1. 自定义视图组件

**BottomNavigationItem.java**
- 位置：`app/src/main/java/com/example/personalaccounting/view/BottomNavigationItem.java`
- 功能：自定义底部导航项组件，支持图标+文本结构
- 继承：`LinearLayout`

### 2. 布局文件

**item_bottom_navigation.xml**
- 位置：`app/src/main/res/layout/item_bottom_navigation.xml`
- 功能：定义单个导航项的布局结构

## 结构化布局设计

### 布局层次结构

```
BottomNavigationItem (LinearLayout)
├── LinearLayout (垂直布局，居中对齐)
│   ├── ImageView (24dp × 24dp，图标)
│   └── TextView (12sp，文本标签)
```

### 尺寸规范

| 元素 | 尺寸 | 说明 |
|------|------|------|
| 导航栏高度 | 56dp | 符合Material Design规范 |
| 图标尺寸 | 24dp × 24dp | 标准图标尺寸 |
| 文本字号 | 12sp | 小字号，节省空间 |
| 上边距 | 8dp | 图标与顶部间距 |
| 图文间距 | 4dp | 图标与文本间距 |
| 下边距 | 4dp | 文本与底部间距 |

### 对齐方式

- **整体布局**：`android:gravity="center"` - 垂直和水平居中
- **图标**：`android:scaleType="fitCenter"` - 保持比例居中
- **文本**：`android:gravity="center"` - 文本居中对齐

## 交互状态设计

### 状态类型

组件支持三种交互状态：

1. **激活状态（Active）**
   - 图标：使用激活图标（黄色系）
   - 文本：使用激活颜色（primary_dark: #FFD54F）
   - 透明度：100%
   - 用途：表示当前选中的导航项

2. **默认状态（Default）**
   - 图标：使用未激活图标（灰色系）
   - 文本：使用默认颜色（neutral_600: #757575）
   - 透明度：100%
   - 用途：表示可点击但未选中的导航项

3. **禁用状态（Disabled）**
   - 图标：使用未激活图标（灰色系）
   - 文本：使用禁用颜色（neutral_400: #BDBDBD）
   - 透明度：50%
   - 用途：表示暂时不可用的导航项

### 状态切换逻辑

```java
private void updateState() {
    if (mIsActive && mIsEnabled) {
        // 激活状态
        mIconView.setImageDrawable(mActiveIcon);
        mLabelView.setSelected(true);
        mLabelView.setEnabled(true);
        setAlpha(1.0f);
    } else if (!mIsEnabled) {
        // 禁用状态
        mIconView.setImageDrawable(mInactiveIcon);
        mLabelView.setSelected(false);
        mLabelView.setEnabled(false);
        setAlpha(0.5f);
    } else {
        // 默认状态
        mIconView.setImageDrawable(mInactiveIcon);
        mLabelView.setSelected(false);
        mLabelView.setEnabled(true);
        setAlpha(1.0f);
    }
}
```

### 颜色选择器

**navigation_text_selector.xml**
```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_selected="true" android:color="@color/primary_dark" />
    <item android:state_enabled="false" android:color="@color/neutral_400" />
    <item android:color="@color/neutral_600" />
</selector>
```

## 响应式行为设计

### 1. 屏幕尺寸适配

#### 小屏幕（< 360dp）
- 导航项宽度：使用 `android:layout_weight="1"` 自动分配
- 图标尺寸：保持 24dp × 24dp
- 文本字号：保持 12sp
- 文本截断：`android:maxLines="1"` + `android:ellipsize="end"`

#### 中等屏幕（360dp - 600dp）
- 导航项宽度：使用 `android:layout_weight="1"` 自动分配
- 图标尺寸：保持 24dp × 24dp
- 文本字号：保持 12sp
- 文本完整显示：通常不需要截断

#### 大屏幕（> 600dp）
- 导航项宽度：使用 `android:layout_weight="1"` 自动分配
- 图标尺寸：可考虑增加到 28dp × 28dp（需额外适配）
- 文本字号：可考虑增加到 14sp（需额外适配）
- 文本完整显示：完整显示

### 2. 方向适配

#### 竖屏模式
- 导航栏高度：56dp
- 布局方向：水平（`android:orientation="horizontal"`）
- 导航项排列：从左到右

#### 横屏模式
- 导航栏高度：56dp（保持一致）
- 布局方向：水平（`android:orientation="horizontal"`）
- 导航项排列：从左到右

### 3. 密度适配

#### 低密度（ldpi, ~120dpi）
- 图标尺寸：24dp × 24dp → 18px × 18px
- 文本字号：12sp → 9px

#### 中密度（mdpi, ~160dpi）
- 图标尺寸：24dp × 24dp → 24px × 24px
- 文本字号：12sp → 12px

#### 高密度（hdpi, ~240dpi）
- 图标尺寸：24dp × 24dp → 36px × 36px
- 文本字号：12sp → 18px

#### 超高密度（xhdpi, ~320dpi）
- 图标尺寸：24dp × 24dp → 48px × 48px
- 文本字号：12sp → 24px

#### 超超高密度（xxhdpi, ~480dpi）
- 图标尺寸：24dp × 24dp → 72px × 72px
- 文本字号：12sp → 36px

## 图标设计

### 图标资源

| 导航项 | 激活图标 | 未激活图标 |
|--------|----------|------------|
| 首页 | ic_home_active.xml | ic_home_inactive.xml |
| 日历 | ic_calendar_active.xml | ic_calendar_inactive.xml |

### 图标规范

- **格式**：Vector Drawable（XML）
- **尺寸**：24dp × 24dp
- **视口**：24 × 24
- **激活颜色**：#FFD54F（primary_dark）
- **未激活颜色**：#9E9E9E（neutral_500）

### 图标示例

**首页图标（激活）**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFD54F"
        android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/>
</vector>
```

## 使用示例

### XML布局中使用

```xml
<LinearLayout
    android:id="@+id/bottom_navigation"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:orientation="horizontal"
    android:background="@color/background_card"
    android:elevation="8dp">

    <com.example.personalaccounting.view.BottomNavigationItem
        android:id="@+id/nav_home"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        app:labelText="首页"
        app:activeIcon="@drawable/ic_home_active"
        app:inactiveIcon="@drawable/ic_home_inactive" />

    <com.example.personalaccounting.view.BottomNavigationItem
        android:id="@+id/nav_calendar"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        app:labelText="日历"
        app:activeIcon="@drawable/ic_calendar_active"
        app:inactiveIcon="@drawable/ic_calendar_inactive" />

</LinearLayout>
```

### Java代码中使用

```java
// 初始化视图
BottomNavigationItem navHome = findViewById(R.id.nav_home);
BottomNavigationItem navCalendar = findViewById(R.id.nav_calendar);

// 设置激活状态
navHome.setActive(true);
navCalendar.setActive(false);

// 设置禁用状态
navHome.setEnabledState(true);
navCalendar.setEnabledState(false);

// 设置点击监听
navHome.setOnClickListener(v -> {
    // 处理首页点击
});

navCalendar.setOnClickListener(v -> {
    // 处理日历点击
});
```

## 自定义属性

### attrs.xml

```xml
<declare-styleable name="BottomNavigationItem">
    <attr name="labelText" format="string" />
    <attr name="activeIcon" format="reference" />
    <attr name="inactiveIcon" format="reference" />
</declare-styleable>
```

### 属性说明

| 属性名 | 类型 | 说明 | 必填 |
|--------|------|------|------|
| labelText | string | 导航项文本标签 | 是 |
| activeIcon | reference | 激活状态图标资源 | 是 |
| inactiveIcon | reference | 未激活状态图标资源 | 是 |

## 可访问性

### 视觉可访问性

- **对比度**：所有文本颜色符合WCAG 2.1 AA级标准
- **图标大小**：24dp × 24dp，确保在所有设备上清晰可见
- **状态反馈**：通过颜色和透明度明确表示不同状态

### 触摸可访问性

- **最小触摸区域**：48dp × 48dp（导航栏高度56dp，满足要求）
- **间距**：导航项之间有足够的间距，避免误触
- **反馈**：使用 `selectableItemBackgroundBorderless` 提供触摸反馈

### 内容描述

- 图标使用 `android:contentDescription="@null"`，因为文本标签已提供描述
- 文本标签使用 `android:maxLines="1"` 和 `android:ellipsize="end"` 确保文本完整显示

## 性能优化

### 1. 布局优化

- 使用 `<merge>` 标签减少布局层级
- 使用 `LinearLayout` 而非 `ConstraintLayout`，减少计算开销

### 2. 资源优化

- 使用 Vector Drawable 而非 PNG，减少APK体积
- 图标复用：激活和未激活使用相同的路径，仅颜色不同

### 3. 状态管理

- 状态切换时只更新必要的属性
- 使用 `setAlpha()` 而非重新加载资源

## 扩展性

### 添加新导航项

1. 创建新的图标资源（激活和未激活）
2. 在布局文件中添加新的 `BottomNavigationItem`
3. 在Activity中初始化和设置监听器

### 自定义样式

可以通过以下方式自定义样式：

1. 修改颜色资源文件
2. 修改图标资源
3. 调整尺寸和间距
4. 添加动画效果

## 测试建议

### 功能测试

- [ ] 点击导航项能够正确切换页面
- [ ] 激活状态正确显示
- [ ] 禁用状态正确显示
- [ ] 文本过长时正确截断

### 响应式测试

- [ ] 在不同屏幕尺寸下测试
- [ ] 在不同屏幕方向下测试
- [ ] 在不同屏幕密度下测试

### 性能测试

- [ ] 测量布局渲染时间
- [ ] 测量状态切换时间
- [ ] 检查内存使用情况

### 可访问性测试

- [ ] 使用TalkBack测试屏幕阅读器支持
- [ ] 测试不同对比度模式
- [ ] 测试不同字体大小设置

## 总结

底部导航栏组件采用了结构化设计，具有以下特点：

1. **清晰的层次结构**：图标在上，文本在下，符合用户习惯
2. **完善的状态管理**：支持激活、默认、禁用三种状态
3. **良好的响应式行为**：适配不同屏幕尺寸、方向和密度
4. **优秀的可访问性**：符合Material Design和WCAG标准
5. **高度可扩展**：易于添加新功能和自定义样式

该组件已在首页和日历页面中成功应用，构建测试通过，可以正常使用。