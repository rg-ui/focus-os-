package com.focusos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusos.app.data.models.TaskItem
import com.focusos.app.data.models.Subject
import com.focusos.app.data.models.PortfolioProject
import com.focusos.app.data.models.InternshipApplication

@Composable
fun GlobalSearchDialog(
    tasks: List<TaskItem>,
    subjects: List<Subject>,
    projects: List<PortfolioProject>,
    internships: List<InternshipApplication>,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredTasks = remember(query, tasks) {
        if (query.isBlank()) emptyList()
        else tasks.filter { it.title.contains(query, ignoreCase = true) }
    }

    val filteredSubjects = remember(query, subjects) {
        if (query.isBlank()) emptyList()
        else subjects.filter { it.name.contains(query, ignoreCase = true) }
    }

    val filteredProjects = remember(query, projects) {
        if (query.isBlank()) emptyList()
        else projects.filter { it.title.contains(query, ignoreCase = true) }
    }

    val filteredInternships = remember(query, internships) {
        if (query.isBlank()) emptyList()
        else internships.filter { it.company.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true) }
    }

    val totalMatches = filteredTasks.size + filteredSubjects.size + filteredProjects.size + filteredInternships.size

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search tasks, subjects, projects...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (query.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Type to search your entire personal OS",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else if (totalMatches == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No results found for '$query'",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (filteredTasks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "TASKS (${filteredTasks.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(filteredTasks) { task ->
                                SearchResultRow(
                                    title = task.title,
                                    subtitle = "Category: ${task.category.name} • ${task.deadline}",
                                    onClick = {
                                        onItemSelected("task_${task.id}")
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (filteredSubjects.isNotEmpty()) {
                            item {
                                Text(
                                    text = "SUBJECTS (${filteredSubjects.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(filteredSubjects) { subject ->
                                SearchResultRow(
                                    title = subject.name,
                                    subtitle = "${subject.degreeType.name} • ${subject.credits} Credits • Score: ${subject.currentScore}%",
                                    onClick = {
                                        onItemSelected("subject_${subject.id}")
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (filteredProjects.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PROJECTS (${filteredProjects.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(filteredProjects) { project ->
                                SearchResultRow(
                                    title = project.title,
                                    subtitle = "${project.status} • ${project.progressPercent}% done",
                                    onClick = {
                                        onItemSelected("project_${project.id}")
                                        onDismiss()
                                    }
                                )
                            }
                        }

                        if (filteredInternships.isNotEmpty()) {
                            item {
                                Text(
                                    text = "INTERNSHIPS (${filteredInternships.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(filteredInternships) { intern ->
                                SearchResultRow(
                                    title = "${intern.role} @ ${intern.company}",
                                    subtitle = "${intern.status.name} • ${intern.stipend}",
                                    onClick = {
                                        onItemSelected("intern_${intern.id}")
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
