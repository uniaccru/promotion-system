import { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Chip,
  CircularProgress,
  Tabs,
  Tab,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material';
import { Add, Edit, Delete, Assignment } from '@mui/icons-material';
import { goalService } from '@/services/goalService';
import { employeeService } from '@/services/employeeService';
import { Goal, GoalTemplate, CreateGoalRequest, AssignGoalRequest, UpdateGoalStatusRequest, Employee } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { format } from 'date-fns';

const GoalsPage = () => {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [goalTemplates, setGoalTemplates] = useState<GoalTemplate[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState(0);
  const [openTemplateDialog, setOpenTemplateDialog] = useState(false);
  const [openAssignDialog, setOpenAssignDialog] = useState(false);
  const [openStatusDialog, setOpenStatusDialog] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<GoalTemplate | null>(null);
  const [selectedGoal, setSelectedGoal] = useState<Goal | null>(null);
  const { user } = useAuth();
  const isHR = user?.role?.toLowerCase() === 'hr';

  const [templateFormData, setTemplateFormData] = useState<CreateGoalRequest>({
    title: '',
    description: '',
    metric: '',
    reviewPeriod: 'Q4-2024',
  });

  const [assignFormData, setAssignFormData] = useState<AssignGoalRequest>({
    goalId: 0,
    employeeId: 0,
    dueDate: format(new Date(), 'yyyy-MM-dd'),
  });

  const [statusFormData, setStatusFormData] = useState<UpdateGoalStatusRequest>({
    status: '',
  });

  useEffect(() => {
    fetchData();
  }, [user]);

  useEffect(() => {
    // Обновляем данные при переключении вкладок для HR
    if (isHR && tabValue === 1) {
      fetchData();
    }
  }, [tabValue, isHR]);

  const fetchData = async () => {
    try {
      setLoading(true);
      if (isHR) {
        const [templates, employeesData, allAssignments] = await Promise.all([
          goalService.getAllGoalTemplates(),
          employeeService.getAllEmployees(),
          goalService.getAllGoalAssignments(),
        ]);
        setGoalTemplates(templates);
        setEmployees(employeesData);
        setGoals(allAssignments);
      } else {
        const myGoals = await goalService.getMyGoals();
        setGoals(myGoals);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenTemplateDialog = (template?: GoalTemplate) => {
    if (template) {
      setEditingTemplate(template);
      setTemplateFormData({
        title: template.title,
        description: template.description,
        metric: template.metric,
        reviewPeriod: template.reviewPeriod,
      });
    } else {
      setEditingTemplate(null);
      setTemplateFormData({
        title: '',
        description: '',
        metric: '',
        reviewPeriod: 'Q4-2024',
      });
    }
    setOpenTemplateDialog(true);
  };

  const handleCloseTemplateDialog = () => {
    setOpenTemplateDialog(false);
    setEditingTemplate(null);
  };

  const handleSubmitTemplate = async () => {
    if (!templateFormData.title.trim() || !templateFormData.description.trim() || !templateFormData.metric.trim()) {
      alert('Пожалуйста, заполните все обязательные поля');
      return;
    }

    try {
      if (editingTemplate) {
        await goalService.updateGoalTemplate(editingTemplate.id, templateFormData);
      } else {
        await goalService.createGoalTemplate(templateFormData);
      }
      fetchData();
      handleCloseTemplateDialog();
    } catch (error: any) {
      console.error('Failed to save goal template:', error);
      alert(error.response?.data?.message || 'Ошибка при сохранении шаблона цели');
    }
  };

  const handleDeleteTemplate = async (id: number) => {
    if (window.confirm('Вы уверены, что хотите удалить этот шаблон цели?')) {
      try {
        await goalService.deleteGoalTemplate(id);
        fetchData();
      } catch (error) {
        console.error('Failed to delete goal template:', error);
        alert('Ошибка при удалении шаблона цели');
      }
    }
  };

  const handleOpenAssignDialog = (template: GoalTemplate) => {
    setAssignFormData({
      goalId: template.id,
      employeeId: 0,
      dueDate: format(new Date(), 'yyyy-MM-dd'),
    });
    setOpenAssignDialog(true);
  };

  const handleCloseAssignDialog = () => {
    setOpenAssignDialog(false);
  };

  const handleSubmitAssign = async () => {
    if (!assignFormData.employeeId || !assignFormData.dueDate) {
      alert('Пожалуйста, выберите сотрудника и дату выполнения');
      return;
    }

    try {
      await goalService.assignGoal(assignFormData);
      fetchData();
      handleCloseAssignDialog();
      alert('Цель успешно назначена сотруднику');
    } catch (error: any) {
      console.error('Failed to assign goal:', error);
      alert(error.response?.data?.message || 'Ошибка при назначении цели');
    }
  };

  const handleOpenStatusDialog = (goal: Goal) => {
    setSelectedGoal(goal);
    setStatusFormData({ status: goal.status });
    setOpenStatusDialog(true);
  };

  const handleCloseStatusDialog = () => {
    setOpenStatusDialog(false);
    setSelectedGoal(null);
  };

  const handleSubmitStatus = async () => {
    if (!selectedGoal) return;

    try {
      await goalService.updateGoalStatus(selectedGoal.id, statusFormData);
      fetchData();
      handleCloseStatusDialog();
    } catch (error: any) {
      console.error('Failed to update goal status:', error);
      alert(error.response?.data?.message || 'Ошибка при обновлении статуса');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed':
      case 'завершено':
        return 'success';
      case 'in_progress':
      case 'в процессе':
        return 'primary';
      case 'not started':
      case 'не начато':
        return 'default';
      case 'blocked':
      case 'заблокировано':
        return 'error';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 3,
          gap: 2,
        }}
      >
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            Управление целями
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Создавайте шаблоны, назначайте задачи и отслеживайте прогресс
          </Typography>
        </Box>
        {isHR && (
          <Button variant="contained" startIcon={<Add />} onClick={() => handleOpenTemplateDialog()}>
            Создать общую цель
          </Button>
        )}
      </Box>

      {isHR ? (
        <Paper>
          <Tabs value={tabValue} onChange={(_e, v) => setTabValue(v)}>
            <Tab label="Общие цели" />
            <Tab label="Назначения" />
          </Tabs>
          <Box p={3}>
            {tabValue === 0 && (
              <Box>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Название</TableCell>
                        <TableCell>Описание</TableCell>
                        <TableCell>Метрика</TableCell>
                        <TableCell>Период ревью</TableCell>
                        <TableCell align="right">Действия</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {goalTemplates.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={5} align="center">
                            Нет общих целей. Создайте первую цель!
                          </TableCell>
                        </TableRow>
                      ) : (
                        goalTemplates.map((template) => (
                          <TableRow key={template.id}>
                            <TableCell>{template.title}</TableCell>
                            <TableCell>{template.description}</TableCell>
                            <TableCell>{template.metric}</TableCell>
                            <TableCell>{template.reviewPeriod}</TableCell>
                            <TableCell align="right">
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => handleOpenAssignDialog(template)}
                                title="Назначить сотруднику"
                              >
                                <Assignment />
                              </IconButton>
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => handleOpenTemplateDialog(template)}
                              >
                                <Edit />
                              </IconButton>
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() => handleDeleteTemplate(template.id)}
                              >
                                <Delete />
                              </IconButton>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
            {tabValue === 1 && (
              <Box>
                <Typography variant="h6" gutterBottom>
                  Назначенные цели сотрудникам
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Сотрудник</TableCell>
                        <TableCell>Название цели</TableCell>
                        <TableCell>Описание</TableCell>
                        <TableCell>Метрика</TableCell>
                        <TableCell>Дата выполнения</TableCell>
                        <TableCell>Статус</TableCell>
                        <TableCell>Период ревью</TableCell>
                        <TableCell align="right">Действия</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {goals.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={8} align="center">
                            Нет назначенных целей. Назначьте цели сотрудникам через вкладку "Общие цели"
                          </TableCell>
                        </TableRow>
                      ) : (
                        goals.map((goal) => (
                          <TableRow key={goal.id}>
                            <TableCell>{goal.employeeName || `ID: ${goal.employeeId}`}</TableCell>
                            <TableCell>{goal.goalTitle}</TableCell>
                            <TableCell>{goal.goalDescription}</TableCell>
                            <TableCell>{goal.goalMetric}</TableCell>
                            <TableCell>{format(new Date(goal.dueDate), 'dd.MM.yyyy')}</TableCell>
                            <TableCell>
                              <Chip label={goal.status} color={getStatusColor(goal.status) as any} size="small" />
                            </TableCell>
                            <TableCell>{goal.reviewPeriod}</TableCell>
                            <TableCell align="right">
                              <IconButton
                                size="small"
                                color="error"
                                onClick={async () => {
                                  if (window.confirm('Вы уверены, что хотите удалить это назначение?')) {
                                    try {
                                      await goalService.deleteGoalAssignment(goal.id);
                                      fetchData();
                                    } catch (error) {
                                      console.error('Failed to delete goal assignment:', error);
                                      alert('Ошибка при удалении назначения');
                                    }
                                  }
                                }}
                              >
                                <Delete />
                              </IconButton>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
          </Box>
        </Paper>
      ) : (
        <Paper>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Название</TableCell>
                  <TableCell>Описание</TableCell>
                  <TableCell>Метрика</TableCell>
                  <TableCell>Дата выполнения</TableCell>
                  <TableCell>Статус</TableCell>
                  <TableCell>Период ревью</TableCell>
                  <TableCell align="right">Действия</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {goals.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      У вас нет назначенных целей
                    </TableCell>
                  </TableRow>
                ) : (
                  goals.map((goal) => (
                    <TableRow key={goal.id}>
                      <TableCell>{goal.goalTitle}</TableCell>
                      <TableCell>{goal.goalDescription}</TableCell>
                      <TableCell>{goal.goalMetric}</TableCell>
                      <TableCell>{format(new Date(goal.dueDate), 'dd.MM.yyyy')}</TableCell>
                      <TableCell>
                        <Chip label={goal.status} color={getStatusColor(goal.status) as any} size="small" />
                      </TableCell>
                      <TableCell>{goal.reviewPeriod}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenStatusDialog(goal)}
                          title="Изменить статус"
                        >
                          <Edit />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Template Dialog */}
      <Dialog open={openTemplateDialog} onClose={handleCloseTemplateDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingTemplate ? 'Редактировать общую цель' : 'Создать общую цель'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Название"
            value={templateFormData.title}
            onChange={(e) => setTemplateFormData({ ...templateFormData, title: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Описание"
            value={templateFormData.description}
            onChange={(e) => setTemplateFormData({ ...templateFormData, description: e.target.value })}
            margin="normal"
            multiline
            rows={3}
            required
          />
          <TextField
            fullWidth
            label="Метрика"
            value={templateFormData.metric}
            onChange={(e) => setTemplateFormData({ ...templateFormData, metric: e.target.value })}
            margin="normal"
            placeholder="например: Завершить 5 проектов, Увеличить продажи на 20%"
            required
          />
          <TextField
            fullWidth
            label="Период ревью"
            value={templateFormData.reviewPeriod}
            onChange={(e) => setTemplateFormData({ ...templateFormData, reviewPeriod: e.target.value })}
            margin="normal"
            placeholder="например: Q4-2024, 2024"
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseTemplateDialog}>Отмена</Button>
          <Button onClick={handleSubmitTemplate} variant="contained" color="primary">
            {editingTemplate ? 'Обновить' : 'Создать'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Assign Dialog */}
      <Dialog open={openAssignDialog} onClose={handleCloseAssignDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Назначить цель сотруднику</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <InputLabel>Сотрудник</InputLabel>
            <Select
              value={assignFormData.employeeId}
              onChange={(e) => setAssignFormData({ ...assignFormData, employeeId: Number(e.target.value) })}
              label="Сотрудник"
            >
              {employees.map((emp) => (
                <MenuItem key={emp.id} value={emp.id}>
                  {emp.fullName}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label="Дата выполнения"
            type="date"
            value={assignFormData.dueDate}
            onChange={(e) => setAssignFormData({ ...assignFormData, dueDate: e.target.value })}
            margin="normal"
            InputLabelProps={{ shrink: true }}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseAssignDialog}>Отмена</Button>
          <Button onClick={handleSubmitAssign} variant="contained" color="primary">
            Назначить
          </Button>
        </DialogActions>
      </Dialog>

      {/* Status Dialog */}
      <Dialog open={openStatusDialog} onClose={handleCloseStatusDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Изменить статус цели</DialogTitle>
        <DialogContent>
          {selectedGoal && (
            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Цель: {selectedGoal.goalTitle}
              </Typography>
              <TextField
                fullWidth
                select
                label="Статус"
                value={statusFormData.status}
                onChange={(e) => setStatusFormData({ status: e.target.value })}
                margin="normal"
                required
              >
                <MenuItem value="not_started">Не начато</MenuItem>
                <MenuItem value="in_progress">В процессе</MenuItem>
                <MenuItem value="blocked">Заблокировано</MenuItem>
                <MenuItem value="completed">Завершено</MenuItem>
              </TextField>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseStatusDialog}>Отмена</Button>
          <Button onClick={handleSubmitStatus} variant="contained" color="primary">
            Обновить
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default GoalsPage;
